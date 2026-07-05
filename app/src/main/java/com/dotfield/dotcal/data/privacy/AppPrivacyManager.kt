package com.dotfield.dotcal.data.privacy

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
import com.dotfield.dotcal.prefs.CalendarPreferences
import com.dotfield.dotcal.prefs.calendarPreferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom

data class AppLockState(
    val enabled: Boolean,
    val hasPin: Boolean,
)

class AppPrivacyManager(
    private val context: Context,
) {
    fun observeAppLockState(): Flow<AppLockState> {
        return context.calendarPreferencesDataStore.data.map { preferences ->
            AppLockState(
                enabled = preferences[CalendarPreferences.KEY_APP_LOCK_ENABLED] ?: false,
                hasPin = !preferences[CalendarPreferences.KEY_APP_LOCK_PIN_HASH].isNullOrBlank() &&
                    !preferences[CalendarPreferences.KEY_APP_LOCK_PIN_SALT].isNullOrBlank(),
            )
        }
    }

    fun observePrivateVaultIds(): Flow<Set<String>> {
        return context.calendarPreferencesDataStore.data.map { preferences ->
            decodeIds(preferences[CalendarPreferences.KEY_PRIVATE_VAULT_EVENT_IDS])
        }
    }

    suspend fun setPin(pin: String) = withContext(Dispatchers.IO) {
        require(pin.isValidPin()) { "PIN must be 4-8 digits" }
        val salt = ByteArray(SALT_BYTES).also { secureRandom.nextBytes(it) }
        val saltText = salt.encode()
        val hash = hashPin(saltText, pin)
        context.calendarPreferencesDataStore.edit { preferences ->
            preferences[CalendarPreferences.KEY_APP_LOCK_PIN_SALT] = saltText
            preferences[CalendarPreferences.KEY_APP_LOCK_PIN_HASH] = hash
            preferences[CalendarPreferences.KEY_APP_LOCK_ENABLED] = true
        }
    }

    suspend fun verifyPin(pin: String): Boolean = withContext(Dispatchers.Default) {
        if (!pin.isValidPin()) return@withContext false
        val preferences = context.calendarPreferencesDataStore.data.first()
        val salt = preferences[CalendarPreferences.KEY_APP_LOCK_PIN_SALT] ?: return@withContext false
        val expected = preferences[CalendarPreferences.KEY_APP_LOCK_PIN_HASH] ?: return@withContext false
        constantTimeEquals(hashPin(salt, pin), expected)
    }

    suspend fun disableAppLock() = withContext(Dispatchers.IO) {
        context.calendarPreferencesDataStore.edit { preferences ->
            preferences[CalendarPreferences.KEY_APP_LOCK_ENABLED] = false
        }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        context.calendarPreferencesDataStore.edit { preferences ->
            val hasPin = !preferences[CalendarPreferences.KEY_APP_LOCK_PIN_HASH].isNullOrBlank() &&
                !preferences[CalendarPreferences.KEY_APP_LOCK_PIN_SALT].isNullOrBlank()
            preferences[CalendarPreferences.KEY_APP_LOCK_ENABLED] = enabled && hasPin
        }
    }

    suspend fun clearPin() = withContext(Dispatchers.IO) {
        context.calendarPreferencesDataStore.edit { preferences ->
            preferences[CalendarPreferences.KEY_APP_LOCK_ENABLED] = false
            preferences.remove(CalendarPreferences.KEY_APP_LOCK_PIN_SALT)
            preferences.remove(CalendarPreferences.KEY_APP_LOCK_PIN_HASH)
        }
    }

    suspend fun addPrivateEvent(eventId: String) = updateVaultIds { it + eventId }

    suspend fun removePrivateEvent(eventId: String) = updateVaultIds { it - eventId }

    private suspend fun updateVaultIds(transform: (Set<String>) -> Set<String>) = withContext(Dispatchers.IO) {
        context.calendarPreferencesDataStore.edit { preferences ->
            val current = decodeIds(preferences[CalendarPreferences.KEY_PRIVATE_VAULT_EVENT_IDS])
            val next = transform(current).filter { it.isNotBlank() }.toSortedSet()
            if (next.isEmpty()) {
                preferences.remove(CalendarPreferences.KEY_PRIVATE_VAULT_EVENT_IDS)
            } else {
                preferences[CalendarPreferences.KEY_PRIVATE_VAULT_EVENT_IDS] = next.joinToString(ID_SEPARATOR)
            }
        }
    }

    private fun hashPin(salt: String, pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("$salt:$pin".toByteArray(Charsets.UTF_8))
        return bytes.encode()
    }

    private fun ByteArray.encode(): String = Base64.encodeToString(this, Base64.NO_WRAP)

    private fun decodeIds(raw: String?): Set<String> {
        if (raw.isNullOrBlank()) return emptySet()
        return raw.split(ID_SEPARATOR).mapNotNull { it.trim().takeIf(String::isNotBlank) }.toSet()
    }

    private fun String.isValidPin(): Boolean = length in 4..8 && all(Char::isDigit)

    private fun constantTimeEquals(a: String, b: String): Boolean {
        val left = a.toByteArray(Charsets.UTF_8)
        val right = b.toByteArray(Charsets.UTF_8)
        return MessageDigest.isEqual(left, right)
    }

    private companion object {
        const val SALT_BYTES = 16
        const val ID_SEPARATOR = "\n"
        val secureRandom = SecureRandom()
    }
}
