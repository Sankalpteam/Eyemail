package app.eyemail.settings.notifications

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eyemail.Ey
import app.eyemail.settings.SectionTitle
import app.eyemail.settings.SettingRow
import app.eyemail.settings.SmallButton

/**
 * The notifications screen.
 *
 * One rule decides everything here: the answer is read from the phone on every draw, and
 * never kept in a copy. A cached copy is what made this screen say "Turn on" after the
 * person had already allowed the popup, so there is no copy any more.
 */
@Composable
fun NotificationsSection(
    state: NotificationsState,
    onChange: (enabled: Boolean, granted: Boolean, blocked: Boolean) -> Unit,
    onTest: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onCheckLast: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val granted = NotificationPermission.isGranted()
    val on = granted || state.enabled

    // The permission can be changed outside the app. Coming back should show the truth.
    androidx.compose.runtime.LaunchedEffect(granted) {
        if (granted && !state.enabled) onRefresh()
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { allowed ->
        // Write the answer down at the same moment it is given, then re-read it.
        onChange(allowed, allowed, !allowed)
        onRefresh()
    }

    Column(modifier = modifier.fillMaxWidth().padding(14.dp)) {
        SectionTitle("Notifications")

        SettingRow(
            "Desktop and mobile alerts",
            "Tell me when a new mail arrives in the inbox",
        ) {
            SmallButton(
                text = when {
                    state.blocked -> "Blocked by the phone"
                    on -> "Turn off"
                    else -> "Turn on"
                },
                enabled = !state.blocked,
                onClick = {
                    when {
                        // Already allowed: this switch is the person's own choice.
                        on -> onChange(false, granted, false)
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        else -> onChange(true, true, false)
                    }
                },
            )
        }

        Text(
            if (on) {
                "On. Alerts arrive even when the app is closed."
            } else {
                "Off. Tap Turn on, and allow the popup Android shows."
            },
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = Ey.Text3,
            modifier = Modifier.padding(top = 4.dp),
        )

        SettingRow("Phone settings", "Open where Android keeps the switches for this app") {
            SmallButton(
                text = "Open",
                onClick = {
                    runCatching {
                        context.startActivity(
                            android.content.Intent(
                                android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS,
                            ).apply {
                                putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            },
                        )
                    }
                },
            )
        }

        SettingRow("Test push", "Ask the server to notify this phone right now") {
            SmallButton(
                text = if (state.testing) "Sending..." else "Send test",
                enabled = !state.testing,
                onClick = { onTest() },
            )
        }

        SettingRow("Last real mail", "What the server did when mail last arrived") {
            SmallButton(
                text = "Check",
                enabled = !state.testing,
                onClick = { onCheckLast() },
            )
        }

        if (state.testResult.isNotBlank()) {
            Text(
                state.testResult,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = if (state.testOk) Ey.Text2 else Ey.Danger,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
