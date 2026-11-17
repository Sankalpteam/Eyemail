package app.eyemail.settings.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eyemail.Ey
import app.eyemail.authentication.Alert
import app.eyemail.settings.SectionTitle
import app.eyemail.settings.SettingRow
import app.eyemail.settings.SmallButton

@Composable
fun AccountSection(
    state: AccountState,
    onName: (String) -> Unit,
    onSendReset: () -> Unit,
    onSignOutAll: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(14.dp)) {
        SectionTitle("Account")

        SettingRow("Display name", "Shown on mail you send") {
            OutlinedTextField(
                value = state.displayName,
                onValueChange = onName,
                singleLine = true,
                placeholder = { Text("Your name", fontSize = 13.sp, color = Ey.Text3) },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Ey.Accent,
                    unfocusedBorderColor = Ey.Border,
                    focusedContainerColor = Ey.Surface,
                    unfocusedContainerColor = Ey.Surface,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        SettingRow("Eyemail address", "Your address is also your account id") {
            Text(state.email.ifBlank { "-" }, fontSize = 12.sp, color = Ey.Text2)
        }

        SettingRow("Recovery email", "Password reset and codes go here") {
            Text(state.recoveryEmail.ifBlank { "not set" }, fontSize = 12.sp, color = Ey.Text2)
        }

        SettingRow("Password", "We mail you a reset link, we never show or store your password") {
            SmallButton(
                text = if (state.sendingReset) "Sending..." else "Send reset link",
                enabled = !state.sendingReset && state.email.isNotBlank(),
                onClick = onSendReset,
            )
        }

        Alert(state.resetError, isError = true)
        Alert(state.resetNote, isError = false)

        SettingRow("Sign out of all devices", "Every phone and laptop must sign in again") {
            SmallButton(
                text = if (state.signingOutAll) "Signing out..." else "Sign out everywhere",
                enabled = !state.signingOutAll && !state.deleting,
                onClick = onSignOutAll,
            )
        }

        SettingRow(
            "Delete my account",
            "Removes the mailbox, its mail and the login. Cannot be undone.",
        ) {
            SmallButton(
                text = if (state.deleting) "Deleting..." else "Delete account",
                enabled = !state.deleting && !state.signingOutAll,
                onClick = onDelete,
            )
        }

        Alert(state.actionError, isError = true)
        Alert(state.actionNote, isError = false)

        Spacer(Modifier.height(4.dp))
    }
}
