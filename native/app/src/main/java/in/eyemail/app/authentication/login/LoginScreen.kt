package in.eyemail.app.authentication.login

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
                title = "Sign in",
                subtitle = "Your business mail, without the bill.",
            )

            Spacer(Modifier.height(24.dp))

            EyField(
                label = "Eyemail address",
                value = state.email,
                onValueChange = viewModel::onEmail,
                placeholder = "yourname@eyemail.in",
                hint = "Type the name only and we add @${BuildConfig.DOMAIN_NAME}.",
                keyboardType = KeyboardType.Email,
                enabled = !state.busy,
            )

            Spacer(Modifier.height(14.dp))

            EyField(
                label = "Password",
                value = state.password,
                onValueChange = viewModel::onPassword,
                placeholder = "Enter your password",
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                isPassword = true,
                enabled = !state.busy,
            )

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onForgot, enabled = !state.busy) {
                Text(
                    "Forgot your password?",
                    fontSize = 12.5.sp,
                    color = Ey.Accent,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(6.dp))
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

            Spacer(Modifier.height(28.dp))
            Text("Powered by Indo Workspace", fontSize = 11.sp, color = Ey.Text3)
            FooterCredit(Modifier.padding(top = 6.dp))
        }
    }
}
