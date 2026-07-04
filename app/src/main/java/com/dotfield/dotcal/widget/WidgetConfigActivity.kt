package com.dotfield.dotcal.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import com.dotfield.dotcal.data.CalendarAccount
import com.dotfield.dotcal.data.DotCalDatabase
import com.dotfield.dotcal.prefs.CalendarPreferences
import com.dotfield.dotcal.prefs.calendarPreferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WidgetConfigActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            WidgetConfigScreen(
                appWidgetId = appWidgetId,
                onSave = { accountId -> saveSelection(accountId) },
                onCancel = { finish() },
            )
        }
    }

    private fun saveSelection(accountId: String?) {
        lifecycleScope.launch {
            val glanceId = GlanceAppWidgetManager(this@WidgetConfigActivity).getGlanceIdBy(appWidgetId)
            updateAppWidgetState(this@WidgetConfigActivity, glanceId) { preferences ->
                if (accountId == null) {
                    preferences.remove(CalendarPreferences.KEY_WIDGET_ACCOUNT_ID)
                } else {
                    preferences[CalendarPreferences.KEY_WIDGET_ACCOUNT_ID] = accountId
                }
            }
            WidgetUpdateWorker.updateNow(this@WidgetConfigActivity)
            val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }
}

@Composable
private fun WidgetConfigScreen(
    appWidgetId: Int,
    onSave: (String?) -> Unit,
    onCancel: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var accounts by remember { mutableStateOf<List<CalendarAccount>>(emptyList()) }
    var isPro by remember { mutableStateOf(false) }
    var selectedAccountId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
        val state = getAppWidgetState<Preferences>(context, PreferencesGlanceStateDefinition, glanceId)
        selectedAccountId = state[CalendarPreferences.KEY_WIDGET_ACCOUNT_ID]
        isPro = context.calendarPreferencesDataStore.data.first()[CalendarPreferences.KEY_IS_PRO] ?: false
        accounts = withContext(Dispatchers.IO) {
            DotCalDatabase.create(context).calendarDao().getAccountsForWidgetConfig()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 24.dp),
    ) {
        Text("Widget Calendar", color = Color.White, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            if (isPro) "Choose which calendar this widget shows." else "Calendar picker is included in DotCal Pro.",
            color = Color(0xFFB3B3B3),
            fontFamily = FontFamily.SansSerif,
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(22.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                WidgetAccountOption(
                    title = "All Calendars",
                    subtitle = "Use visible calendars",
                    color = Color(0xFFFF3B30),
                    selected = selectedAccountId == null,
                    enabled = true,
                    onClick = { selectedAccountId = null },
                )
            }
            items(accounts, key = { it.id }) { account ->
                WidgetAccountOption(
                    title = account.displayName.ifBlank { account.accountName },
                    subtitle = account.accountType,
                    color = runCatching { Color(android.graphics.Color.parseColor(account.color)) }.getOrDefault(Color(0xFFFF3B30)),
                    selected = selectedAccountId == account.id,
                    enabled = isPro,
                    onClick = { selectedAccountId = account.id },
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E), contentColor = Color.White),
                modifier = Modifier.weight(1f),
            ) {
                Text("Cancel")
            }
            Button(
                onClick = { onSave(if (isPro) selectedAccountId else null) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30), contentColor = Color.White),
                modifier = Modifier.weight(1f),
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun WidgetAccountOption(
    title: String,
    subtitle: String,
    color: Color,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(if (enabled) color else Color(0xFF555555)),
        )
        Spacer(Modifier.size(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = if (enabled) Color.White else Color(0xFF777777),
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            )
            Text(
                subtitle,
                color = if (enabled) Color(0xFFB3B3B3) else Color(0xFF555555),
                fontFamily = FontFamily.SansSerif,
                fontSize = 12.sp,
            )
        }
        RadioButton(
            selected = selected,
            onClick = onClick,
            enabled = enabled,
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF3B30), unselectedColor = Color(0xFF777777)),
        )
    }
}
