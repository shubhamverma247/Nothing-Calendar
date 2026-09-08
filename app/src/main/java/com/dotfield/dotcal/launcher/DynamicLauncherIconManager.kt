package com.dotfield.dotcal.launcher

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import java.time.LocalDate

private const val TAG = "DynamicLauncherIcon"
private const val ALIAS_PACKAGE = "com.dotfield.dotcal.launcher"
private const val ALIAS_PREFIX = "LauncherDay"
private const val DEFAULT_ENABLED_ALIAS = "$ALIAS_PACKAGE.${ALIAS_PREFIX}01"
private const val FIRST_DAY = 1
private const val LAST_DAY = 31

internal fun launcherAliasClassName(day: Int): String? {
    if (day !in FIRST_DAY..LAST_DAY) return null
    return "$ALIAS_PACKAGE.$ALIAS_PREFIX${day.toString().padStart(2, '0')}"
}

internal fun launcherAliasClassNames(): List<String> = (FIRST_DAY..LAST_DAY).mapNotNull(::launcherAliasClassName)

internal fun localDayOfMonth(date: LocalDate): Int = date.dayOfMonth

class DynamicLauncherIconManager(context: Context) {
    private val appContext = context.applicationContext
    private val packageManager = appContext.packageManager
    private val packageName = appContext.packageName

    fun updateIconForToday(today: LocalDate = LocalDate.now()): Boolean {
        return updateIconForDay(localDayOfMonth(today))
    }

    private fun updateIconForDay(activeDay: Int): Boolean {
        val activeAlias = launcherAliasClassName(activeDay) ?: return false
        val aliases = launcherAliasClassNames().map { alias ->
            AliasState(ComponentName(packageName, alias), isEnabledByPackageManager(alias))
        }
        val alreadyCorrect = aliases.all { state ->
            state.isEnabled == (state.component.className == activeAlias)
        }
        if (alreadyCorrect) return false

        return runCatching {
            val activeComponent = ComponentName(packageName, activeAlias)
            setState(activeComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
            aliases.asSequence()
                .map(AliasState::component)
                .filter { it != activeComponent }
                .forEach { component ->
                    setState(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
                }
            true
        }.onFailure { error ->
            Log.w(TAG, "Unable to update launcher icon", error)
        }.getOrDefault(false)
    }

    private fun isEnabledByPackageManager(aliasClassName: String): Boolean {
        val component = ComponentName(packageName, aliasClassName)
        return when (packageManager.getComponentEnabledSetting(component)) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> false
            else -> aliasClassName == DEFAULT_ENABLED_ALIAS
        }
    }

    private fun setState(component: ComponentName, state: Int) {
        packageManager.setComponentEnabledSetting(
            component,
            state,
            PackageManager.DONT_KILL_APP,
        )
    }

    private data class AliasState(
        val component: ComponentName,
        val isEnabled: Boolean,
    )
}
