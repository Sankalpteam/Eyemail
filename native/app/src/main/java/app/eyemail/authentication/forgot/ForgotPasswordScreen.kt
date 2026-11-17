package app.eyemail.authentication.forgot

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
import app.eyemail.authentication.PrimaryButton

/**
 * Reset password. We never show or set a password here: the server mails a link
 * to the recovery address on the account, which is the only place it can go.
 */
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
                label = "Your Eyemail address",
                value = state.email,
                onValueChange = viewModel::onEmail,
                placeholder = "yourname@eyemail.in",
                hint = "Type the name only and we add @${BuildConfig.DOMAIN_NAME}.",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
                enabled = !state.busy,
            )

            Spacer(Modifier.height(12.dp))
            Alert(state.error, isError = true)
            Alert(state.done, isError = false)
            Spacer(Modifier.height(12.dp))

            PrimaryButton(
                text = if (state.busy) "Sending link..." else "Send reset link",
                loading = state.busy,
                enabled = state.canSubmit,
                onClick = viewModel::send,
            )

            Spacer(Modifier.height(14.dp))

            TextButton(onClick = onBack, enabled = !state.busy) {
                Text(
                    "Back to sign in",
                    fontSize = 12.5.sp,
                    color = Ey.Accent,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(18.dp))
            FooterCredit()
        }
    }
}
