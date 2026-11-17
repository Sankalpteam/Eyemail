package in.eyemail.app.authentication.signup

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import in.eyemail.app.BuildConfig
import in.eyemail.app.Ey
import in.eyemail.app.authentication.Alert
import in.eyemail.app.authentication.BrandHeader
import in.eyemail.app.authentication.EyField
import in.eyemail.app.authentication.FooterCredit
import in.eyemail.app.authentication.LinkRow
import in.eyemail.app.authentication.PrimaryButton

/**
 * Create an account, then prove the recovery mail with the six digit code,
 * exactly the two steps the web signup runs.
 */
@Composable
fun SignUpScreen(
    onSignIn: () -> Unit,
    onSignedIn: () -> Unit,
    viewModel: SignUpViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val signedIn by viewModel.signedIn.collectAsStateWithLifecycle()

    LaunchedEffect(signedIn) {
        if (signedIn) onSignedIn()
    }

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
                title = if (state.step == SignUpState.Step.Form) {
                    "Create your Eyemail"
                } else {
                    "Check your recovery mail"
                },
                subtitle = if (state.step == SignUpState.Step.Form) {
                    "One address that looks like a business, free on the free plan."
                } else {
                    "We sent a 6-digit code to ${state.pendingMasked}. It expires in 10 minutes."
                },
            )

            Spacer(Modifier.height(24.dp))

            if (state.step == SignUpState.Step.Form) {
                EyField(
                    label = "Full name",
                    value = state.name,
                    onValueChange = viewModel::onName,
                    placeholder = "e.g. Yashwanth P",
                    enabled = !state.busy,
                )
                Spacer(Modifier.height(14.dp))
                EyField(
                    label = "Your Eyemail address",
                    value = state.username,
                    onValueChange = viewModel::onUsername,
                    placeholder = "yourname",
                    hint = "Your address will be ${state.username.ifBlank { "yourname" }}@${BuildConfig.DOMAIN_NAME}",
                    enabled = !state.busy,
                )
                Spacer(Modifier.height(14.dp))
                EyField(
                    label = "Recovery email",
                    value = state.recovery,
                    onValueChange = viewModel::onRecovery,
                    placeholder = "you@gmail.com",
                    hint = "A code at signup, and where a password reset goes later.",
                    keyboardType = KeyboardType.Email,
                    enabled = !state.busy,
                )
                Spacer(Modifier.height(14.dp))
                EyField(
                    label = "Password",
                    value = state.password,
                    onValueChange = viewModel::onPassword,
                    placeholder = "Minimum 6 characters",
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    isPassword = true,
                    enabled = !state.busy,
                )

                Spacer(Modifier.height(10.dp))
                Alert(state.error, isError = true)
                Spacer(Modifier.height(12.dp))

                PrimaryButton(
                    text = if (state.busy) "Creating your account..." else "Create account",
                    loading = state.busy,
                    enabled = state.canCreate,
                    onClick = viewModel::createAccount,
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
                    value = state.otp,
                    onValueChange = viewModel::onOtp,
                    placeholder = "000000",
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done,
                    enabled = !state.busy,
                )

                Spacer(Modifier.height(10.dp))
                Alert(state.error, isError = true)
                Alert(state.note, isError = false)
                Spacer(Modifier.height(12.dp))

                PrimaryButton(
                    text = if (state.busy) "Checking..." else "Verify and open my inbox",
                    loading = state.busy,
                    enabled = state.canVerify,
                    onClick = viewModel::verifyCode,
                )

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(onClick = viewModel::resendCode, enabled = !state.busy) {
                        Text(
                            "Send the code again",
                            fontSize = 12.5.sp,
                            color = Ey.Accent,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    TextButton(onClick = viewModel::backToForm, enabled = !state.busy) {
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
