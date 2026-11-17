package in.eyemail.app.settings.account

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
import in.eyemail.app.Ey
import in.eyemail.app.authentication.Alert
import in.eyemail.app.settings.SectionTitle
import in.eyemail.app.settings.SettingRow
import in.eyemail.app.settings.SmallButton
import in.eyemail.app.settings.Tag

@Composable
fun AccountSection(
    state: AccountState,
    onName: (String) -> Unit,
    onSendReset: () -> Unit,
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

        SettingRow("Free plan", "1 GB storage, 50 outside mails a day, 20 recipients a message") {
            Tag("NO CARD")
        }

        Spacer(Modifier.height(4.dp))
    }
}
