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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eyemail.Ey
import app.eyemail.settings.SectionTitle
import app.eyemail.settings.SettingRow
import app.eyemail.settings.SmallButton

@Composable
fun NotificationsSection(
    state: NotificationsState,
    onChange: (enabled: Boolean, granted: Boolean, blocked: Boolean) -> Unit,
    onTest: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // Android 13 and up ask at runtime; below that the permission is given at install.
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        onChange(granted, granted, !granted)
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
                    state.enabled -> "Turn off"
                    state.permissionGranted -> "Turn on"
                    else -> "Enable notifications"
                },
                enabled = !state.blocked,
                onClick = {
                    if (state.enabled) {
                        onChange(false, state.permissionGranted, false)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        onChange(true, true, false)
                    }
                },
            )
        }

        Text(
            "Alerts arrive even when the app is closed. If one does not come, tap the test below " +
                "and the answer will tell you which side is at fault.",
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = Ey.Text3,
            modifier = Modifier.padding(top = 4.dp),
        )

        SettingRow("Test push", "Ask the server to notify this phone right now") {
            SmallButton(
                text = if (state.testing) "Sending..." else "Send test",
                enabled = !state.testing,
                onClick = { onTest() },
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
