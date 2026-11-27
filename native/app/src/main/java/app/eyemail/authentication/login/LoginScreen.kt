package app.eyemail.authentication.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
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
 * Sign in. Same words, same order, same colours as the web sign-in page:
 * address, password, one button, then the two ways out of it.
 */
@Composable
fun LoginScreen(
    onSignup: () -> Unit,
    onForgot: () -> Unit,
    onSignedIn: () -> Unit,
    viewModel: LoginViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
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
                title = "Sign in",
                subtitle = "Enter your @${BuildConfig.DOMAIN_NAME} address and password.",
            )

            Spacer(Modifier.height(24.dp))

            val emailFocus = androidx.compose.runtime.remember { androidx.compose.ui.focus.FocusRequester() }
            val passwordFocus = androidx.compose.runtime.remember { androidx.compose.ui.focus.FocusRequester() }

            // The cursor is in the address field when the screen opens, so the keyboard is
            // ready and nobody has to tap twice to start typing.
            androidx.compose.runtime.LaunchedEffect(Unit) {
                runCatching { emailFocus.requestFocus() }
            }

            EyField(
                label = "Eyemail address",
                modifier = Modifier.focusRequester(emailFocus),
                value = state.email,
                onValueChange = viewModel::onEmail,
                placeholder = "yourname@eyemail.in",
                hint = "Type the name only and we add @${BuildConfig.DOMAIN_NAME}.",
                keyboardType = KeyboardType.Email,
                enabled = !state.busy,
                onNext = { passwordFocus.requestFocus() },
            )

            Spacer(Modifier.height(14.dp))

            EyField(
                label = "Password",
                modifier = Modifier.focusRequester(passwordFocus),
                value = state.password,
                onValueChange = viewModel::onPassword,
                placeholder = "Enter your password",
                hint = if (state.password.isNotEmpty() && state.password.length < 6) {
                    "Passwords are at least 6 characters."
                } else {
                    null
                },
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                onImeAction = viewModel::signIn,
                isPassword = true,
                enabled = !state.busy,
                labelAction = {
                    Text(
                        "Forgot password?",
                        fontSize = 12.5.sp,
                        color = Ey.Accent,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable(enabled = !state.busy, onClick = onForgot),
                    )
                },
            )

            Spacer(Modifier.height(14.dp))
            Alert(state.error, isError = true)
            Spacer(Modifier.height(10.dp))

            PrimaryButton(
                text = if (state.busy) "Signing in..." else "Sign in",
                loading = state.busy,
                enabled = state.canSubmit,
                onClick = viewModel::signIn,
            )

            Spacer(Modifier.height(18.dp))

            LinkRow(
                prompt = "New here?",
                action = "Create your Eyemail",
                onClick = onSignup,
            )

            Spacer(Modifier.height(18.dp))
            FooterCredit()
        }
    }
}
