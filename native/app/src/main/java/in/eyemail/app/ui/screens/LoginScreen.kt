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
import androidx.compose.material3.MaterialTheme
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
import in.eyemail.app.data.AuthRepository.Session
import in.eyemail.app.ui.components.Alert
import in.eyemail.app.ui.components.BrandHeader
import in.eyemail.app.ui.components.EyField
import in.eyemail.app.ui.components.FooterCredit
import in.eyemail.app.ui.components.LinkRow
import in.eyemail.app.ui.components.PrimaryButton
import in.eyemail.app.ui.theme.Ey
import kotlinx.coroutines.launch

/**
 * Sign in. Same words, same order, same colours as the web sign-in page:
 * address, password, one button, then the two ways out of it.
 */
@Composable
fun LoginScreen(
    onSignup: () -> Unit,
    onForgot: () -> Unit,
    onSignedIn: (Session) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
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
                title = "Sign in",
                subtitle = "Your business mail, without the bill.",
            )

            Spacer(Modifier.height(24.dp))

            EyField(
                label = "Eyemail address",
                value = email,
                onValueChange = { email = it; error = "" },
                placeholder = "yourname@eyemail.in",
                hint = "Type the name only and we add @${BuildConfig.DOMAIN_NAME}.",
                keyboardType = KeyboardType.Email,
                enabled = !busy,
            )

            Spacer(Modifier.height(14.dp))

            EyField(
                label = "Password",
                value = password,
                onValueChange = { password = it; error = "" },
                placeholder = "Enter your password",
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                isPassword = true,
                enabled = !busy,
            )

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onForgot, enabled = !busy) {
                Text("Forgot your password?", fontSize = 12.5.sp, color = Ey.Accent, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(6.dp))
            Alert(error, isError = true)
            Spacer(Modifier.height(10.dp))

            PrimaryButton(
                text = if (busy) "Signing in..." else "Sign in",
                loading = busy,
                enabled = email.isNotBlank() && password.isNotBlank(),
                onClick = {
                    if (busy) return@PrimaryButton
                    busy = true
                    error = ""
                    scope.launch {
                        try {
                            val address = if (email.contains("@")) {
                                email.trim().lowercase()
                            } else {
                                "${email.trim().lowercase()}@${BuildConfig.DOMAIN_NAME}"
                            }
                            val session = AuthRepository.signIn(address, password)
                            busy = false
                            onSignedIn(session)
                        } catch (e: Exception) {
                            busy = false
                            error = e.message ?: "Could not sign in."
                        }
                    }
                },
            )

            Spacer(Modifier.height(18.dp))

            LinkRow(
                prompt = "New here?",
                action = "Create your Eyemail",
                onClick = onSignup,
            )

            Spacer(Modifier.height(28.dp))

            Text(
                "Powered by Indo Workspace",
                fontSize = 11.sp,
                color = Ey.Text3,
                modifier = Modifier.fillMaxSize(),
            )
            FooterCredit(Modifier.padding(top = 6.dp))
        }
    }
}
