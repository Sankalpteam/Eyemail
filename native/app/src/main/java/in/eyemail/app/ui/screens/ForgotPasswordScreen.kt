package in.eyemail.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import in.eyemail.app.BuildConfig
import in.eyemail.app.data.AuthRepository
import in.eyemail.app.ui.components.Alert
import in.eyemail.app.ui.components.BrandHeader
import in.eyemail.app.ui.components.EyField
import in.eyemail.app.ui.components.FooterCredit
import in.eyemail.app.ui.components.PrimaryButton
import in.eyemail.app.ui.theme.Ey
import kotlinx.coroutines.launch

/**
 * Reset password. We never show or set a password here: the server mails a link
 * to the recovery address on the account, which is the only place it can go.
 */
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var done by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
        ) {
            BrandHeader(
                title = "Reset password",
                subtitle = "Type your Eyemail address. The reset link goes to the recovery mail on that account.",
            )

            Spacer(Modifier.height(24.dp))

            EyField(
                label = "Eyemail address",
                value = email,
                onValueChange = { email = it; error = ""; done = "" },
                placeholder = "yourname@eyemail.in",
                hint = "Type the name only and we add @${BuildConfig.DOMAIN_NAME}.",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
                enabled = !busy,
            )

            Spacer(Modifier.height(12.dp))
            Alert(error, isError = true)
            Alert(done, isError = false)
            Spacer(Modifier.height(12.dp))

            PrimaryButton(
                text = if (busy) "Sending link..." else "Send reset link",
                loading = busy,
                enabled = email.isNotBlank(),
                onClick = {
                    if (busy) return@PrimaryButton
                    busy = true
                    error = ""
                    done = ""
                    scope.launch {
                        try {
                            val address = if (email.contains("@")) {
                                email.trim().lowercase()
                            } else {
                                "${email.trim().lowercase()}@${BuildConfig.DOMAIN_NAME}"
                            }
                            val masked = AuthRepository.forgotPassword(address)
                            busy = false
                            done = "Reset link sent to $masked. Check Inbox and Spam."
                        } catch (e: Exception) {
                            busy = false
                            error = e.message ?: "Could not send the reset mail."
                        }
                    }
                },
            )

            Spacer(Modifier.height(14.dp))

            TextButton(onClick = onBack, enabled = !busy) {
                Text("Back to sign in", fontSize = 12.5.sp, color = Ey.Accent, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(28.dp))
            Text("Powered by Indo Workspace", fontSize = 11.sp, color = Ey.Text3)
            FooterCredit(Modifier.padding(top = 6.dp))
        }
    }
}
