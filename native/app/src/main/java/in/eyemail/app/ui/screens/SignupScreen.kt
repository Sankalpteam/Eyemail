package in.eyemail.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
 * Create an account, then prove the recovery mail with the six digit code,
 * exactly the two steps the web signup runs.
 */
@Composable
fun SignupScreen(
    onSignIn: () -> Unit,
    onSignedIn: (Session) -> Unit,
) {
    var step by remember { mutableStateOf(1) }        // 1 form, 2 code

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var recovery by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }

    var pendingEmail by remember { mutableStateOf("") }
    var pendingMasked by remember { mutableStateOf("") }
    var pendingToken by remember { mutableStateOf("") }

    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
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
                title = if (step == 1) "Create your Eyemail" else "Check your recovery mail",
                subtitle = if (step == 1) {
                    "One address that looks like a business, free on the free plan."
                } else {
                    "We sent a 6-digit code to $pendingMasked. It expires in 10 minutes."
                },
            )

            Spacer(Modifier.height(24.dp))

            if (step == 1) {
                EyField(
                    label = "Full name",
                    value = name,
                    onValueChange = { name = it; error = "" },
                    placeholder = "e.g. Yashwanth P",
                    enabled = !busy,
                )
                Spacer(Modifier.height(14.dp))
                EyField(
                    label = "Your Eyemail address",
                    value = username,
                    onValueChange = { raw ->
                        username = raw.lowercase().filter { it.isLetterOrDigit() || it == '.' || it == '_' }
                        error = ""
                    },
                    placeholder = "yourname",
                    hint = "Your address will be ${username.ifBlank { "yourname" }}@${BuildConfig.DOMAIN_NAME}",
                    enabled = !busy,
                )
                Spacer(Modifier.height(14.dp))
                EyField(
                    label = "Recovery email",
                    value = recovery,
                    onValueChange = { recovery = it; error = "" },
                    placeholder = "you@gmail.com",
                    hint = "A code at signup, and where a password reset goes later.",
                    keyboardType = KeyboardType.Email,
                    enabled = !busy,
                )
                Spacer(Modifier.height(14.dp))
                EyField(
                    label = "Password",
                    value = password,
                    onValueChange = { password = it; error = "" },
                    placeholder = "Minimum 6 characters",
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    isPassword = true,
                    enabled = !busy,
                )

                Spacer(Modifier.height(10.dp))
                Alert(error, isError = true)
                Spacer(Modifier.height(12.dp))

                PrimaryButton(
                    text = if (busy) "Creating your account..." else "Create account",
                    loading = busy,
                    enabled = name.isNotBlank() && username.isNotBlank() &&
                        recovery.contains("@") && password.length >= 6,
                    onClick = {
                        if (busy) return@PrimaryButton
                        busy = true
                        error = ""
                        scope.launch {
                            try {
                                val result = AuthRepository.signup(
                                    username = username.trim(),
                                    name = name.trim(),
                                    recoveryEmail = recovery.trim().lowercase(),
                                    password = password,
                                )
                                pendingEmail = result.email
                                pendingMasked = result.maskedRecoveryEmail
                                // The server picked the uid, so sign in to get a token for the next step.
                                pendingToken = AuthRepository.signIn(result.email, password).idToken
                                busy = false
                                step = 2
                            } catch (e: Exception) {
                                busy = false
                                error = e.message ?: "Could not create the account."
                            }
                        }
                    },
                )

                Spacer(Modifier.height(16.dp))
                LinkRow(
                    prompt = "Already have one?",
                    action = "Sign in",
                    onClick = onSignIn,
                )
            } else {
                EyField(
                    label = "6-digit code",
                    value = otp,
                    onValueChange = { raw -> otp = raw.filter { it.isDigit() }.take(6); error = "" },
                    placeholder = "000000",
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done,
                    enabled = !busy,
                )

                Spacer(Modifier.height(10.dp))
                Alert(error, isError = true)
                Alert(note, isError = false)
                Spacer(Modifier.height(12.dp))

                PrimaryButton(
                    text = if (busy) "Checking..." else "Verify and open my inbox",
                    loading = busy,
                    enabled = otp.length == 6,
                    onClick = {
                        if (busy) return@PrimaryButton
                        busy = true
                        error = ""
                        note = ""
                        scope.launch {
                            try {
                                AuthRepository.verifySignupOtp(otp, pendingToken)
                                AuthRepository.registerMailbox(
                                    username = pendingEmail.substringBefore("@"),
                                    name = name.trim(),
                                    recoveryEmail = recovery.trim().lowercase(),
                                    idToken = pendingToken,
                                )
                                val session = AuthRepository.signIn(pendingEmail, password)
                                busy = false
                                onSignedIn(session)
                            } catch (e: Exception) {
                                busy = false
                                error = e.message ?: "That code did not work."
                            }
                        }
                    },
                )

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        enabled = !busy,
                        onClick = {
                            if (busy) return@TextButton
                            busy = true
                            note = ""
                            error = ""
                            scope.launch {
                                try {
                                    AuthRepository.sendSignupOtp(recovery.trim().lowercase())
                                    note = "A new code is on its way."
                                } catch (e: Exception) {
                                    error = e.message ?: "Could not send another code."
                                }
                                busy = false
                            }
                        },
                    ) {
                        Text("Send the code again", fontSize = 12.5.sp, color = Ey.Accent, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = { step = 1; error = ""; otp = "" }, enabled = !busy) {
                        Text("Change details", fontSize = 12.5.sp, color = Ey.Text3)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            Text("Powered by Indo Workspace", fontSize = 11.sp, color = Ey.Text3)
            FooterCredit(Modifier.padding(top = 6.dp))
        }
    }
}
