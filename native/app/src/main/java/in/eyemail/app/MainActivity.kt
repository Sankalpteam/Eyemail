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
import in.eyemail.app.authentication.AuthSessionStore
import in.eyemail.app.home.HomeScreen
import in.eyemail.app.authentication.EyemailMark
import in.eyemail.app.authentication.forgot.ForgotPasswordScreen
import in.eyemail.app.authentication.login.LoginScreen
import in.eyemail.app.authentication.signup.SignUpScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthSessionStore.init(this)
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

private enum class Screen { Login, SignUp, Forgot, SignedIn }

@Composable
private fun EyemailApp() {
    var screen by rememberSaveable { mutableStateOf(Screen.Login) }
    var sessionEmail by rememberSaveable { mutableStateOf("") }

    // The back gesture steps inside the app and never drops the person out of it.
    BackHandler(enabled = screen == Screen.SignUp || screen == Screen.Forgot) {
        screen = Screen.Login
    }

    when (screen) {
        Screen.Login -> LoginScreen(
            onSignup = { screen = Screen.SignUp },
            onForgot = { screen = Screen.Forgot },
            onSignedIn = {
                sessionEmail = AuthSessionStore.email().orEmpty()
                screen = Screen.SignedIn
            },
        )

        Screen.SignUp -> SignUpScreen(
            onSignIn = { screen = Screen.Login },
            onSignedIn = {
                sessionEmail = AuthSessionStore.email().orEmpty()
                screen = Screen.SignedIn
            },
        )

        Screen.Forgot -> ForgotPasswordScreen(onBack = { screen = Screen.Login })

        Screen.SignedIn -> HomeScreen(
            onSignOut = {
                AuthSessionStore.clear()
                sessionEmail = ""
                screen = Screen.Login
            },
            onSettings = { /* settings/ is the next folder */ },
        )
    }
}
