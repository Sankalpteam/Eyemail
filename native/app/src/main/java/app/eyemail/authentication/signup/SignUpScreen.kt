package app.eyemail.authentication.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.eyemail.BuildConfig
import app.eyemail.Ey
import app.eyemail.authentication.Alert
import app.eyemail.authentication.BrandHeader
import app.eyemail.authentication.EyField
import app.eyemail.authentication.FooterCredit
import app.eyemail.authentication.LinkRow
import app.eyemail.authentication.PrimaryButton

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

    // A legal page opened from the box. Null shows the form.
    var openPage by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf<Pair<String, String>?>(null)
    }
    val signedIn by viewModel.signedIn.collectAsStateWithLifecycle()

    LaunchedEffect(signedIn) {
        if (signedIn) {
            viewModel.consumeSignedIn()
            onSignedIn()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.reset()
    }

    val page = openPage
    if (page != null) {
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier.fillMaxSize().background(app.eyemail.Ey.Bg),
        ) {
            app.eyemail.authentication.PageHeader(title = page.second, onBack = { openPage = null })
            androidx.compose.foundation.layout.Box(
                modifier = androidx.compose.ui.Modifier.weight(1f),
            ) {
                app.eyemail.settings.WebPageView(
                    url = page.first + "?inapp=1",
                    title = page.second,
                    onBack = { openPage = null },
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
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
                    "Pick your address, add a recovery mail, and you are in."
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
                    // An address is lower case. The web already says so; the phone's keyboard
                    // kept capitalising the first letter and the field kept undoing it.
                    capitalize = false,
                    enabled = !state.busy,
                )
                Spacer(Modifier.height(14.dp))
                EyField(
                    label = "Recovery email (code at signup, password reset later)",
                    value = state.recovery,
                    onValueChange = viewModel::onRecovery,
                    placeholder = "you@gmail.com",
                    hint = null,
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
                    onImeAction = viewModel::createAccount,
                )

                Spacer(Modifier.height(10.dp))
                Alert(state.error, isError = true)
                Spacer(Modifier.height(12.dp))

                app.eyemail.authentication.TermsBox(
                    checked = state.agreed,
                    onChange = viewModel::onAgree,
                    onOpenTerms = { openPage = app.eyemail.settings.terms_of_use.TermsOfUsePage.URL to "Terms of Use" },
                    onOpenPrivacy = { openPage = app.eyemail.settings.privacy_policy.PrivacyPolicyPage.URL to "Privacy Policy" },
                )
                Spacer(Modifier.height(8.dp))

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
                    onImeAction = viewModel::verifyCode,
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

            Spacer(Modifier.height(18.dp))
            FooterCredit()
        }
    }
}
