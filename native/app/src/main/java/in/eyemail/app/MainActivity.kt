package in.eyemail.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import in.eyemail.app.data.AuthRepository.Session
import in.eyemail.app.ui.components.BrandHeader
import in.eyemail.app.ui.components.EyemailMark
import in.eyemail.app.ui.screens.ForgotPasswordScreen
import in.eyemail.app.ui.screens.LoginScreen
import in.eyemail.app.ui.screens.SignupScreen
import in.eyemail.app.ui.theme.Ey
import in.eyemail.app.ui.theme.EyemailTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EyemailTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Ey.Bg) {
                    EyemailApp()
                }
            }
        }
    }
}

private enum class Screen { Login, Signup, Forgot, SignedIn }

@Composable
private fun EyemailApp() {
    var screen by rememberSaveable { mutableStateOf(Screen.Login) }
    var sessionEmail by rememberSaveable { mutableStateOf("") }

    // The back gesture steps inside the app and never drops the person out of it.
    BackHandler(enabled = screen != Screen.Login && screen != Screen.SignedIn) {
        screen = Screen.Login
    }

    when (screen) {
        Screen.Login -> LoginScreen(
            onSignup = { screen = Screen.Signup },
            onForgot = { screen = Screen.Forgot },
            onSignedIn = { session: Session ->
                sessionEmail = session.email
                screen = Screen.SignedIn
            },
        )

        Screen.Signup -> SignupScreen(
            onSignIn = { screen = Screen.Login },
            onSignedIn = { session: Session ->
                sessionEmail = session.email
                screen = Screen.SignedIn
            },
        )

        Screen.Forgot -> ForgotPasswordScreen(onBack = { screen = Screen.Login })

        Screen.SignedIn -> SignedInPlaceholder(
            email = sessionEmail,
            onSignOut = {
                sessionEmail = ""
                screen = Screen.Login
            },
        )
    }
}

/**
 * Where the inbox will land. It exists so signing in finishes somewhere honest
 * instead of a blank screen while the mailbox screens are being built.
 */
@Composable
private fun SignedInPlaceholder(email: String, onSignOut: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ey.Bg)
            .padding(horizontal = 22.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        EyemailMark(size = 52)
        Spacer(Modifier.height(18.dp))
        Text("You are signed in", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Ey.Text)
        Spacer(Modifier.height(6.dp))
        Text(email, fontSize = 13.sp, color = Ey.Text2)
        Spacer(Modifier.height(18.dp))
        Text(
            "The inbox, reader and settings screens come next.",
            fontSize = 12.5.sp,
            color = Ey.Text3,
        )
        Spacer(Modifier.height(20.dp))
        TextButton(onClick = onSignOut) {
            Text("Sign out", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ey.Accent)
        }
    }
}
