package app.eyemail

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import app.eyemail.authentication.AuthSessionStore
import app.eyemail.home.HomeScreen
import app.eyemail.settings.SettingsScreen
import app.eyemail.settings.SettingsStore
import app.eyemail.settings.ThemeState
import app.eyemail.authentication.EyemailMark
import app.eyemail.authentication.forgot.ForgotPasswordScreen
import app.eyemail.authentication.login.LoginScreen
import app.eyemail.authentication.signup.SignUpScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // The splash reads the theme, so it is installed before anything is drawn.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        AuthSessionStore.init(this)
        SettingsStore.init(this)
        ThemeState.load()
        app.eyemail.settings.notifications.NotificationPermission.init(this)
        app.eyemail.home.Notifier.init(this)
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

private enum class Screen { Login, SignUp, Forgot, SignedIn, Settings }

@Composable
private fun EyemailApp() {
    // A restored screen must not point at a mailbox we no longer have a session for.
    var screen by rememberSaveable {
        mutableStateOf(if (AuthSessionStore.isSignedIn()) Screen.SignedIn else Screen.Login)
    }
    var sessionEmail by rememberSaveable { mutableStateOf("") }

    // The back gesture steps inside the app and never drops the person out of it.
    BackHandler(enabled = screen == Screen.SignUp || screen == Screen.Forgot || screen == Screen.Settings) {
        screen = if (screen == Screen.Settings) Screen.SignedIn else Screen.Login
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

        Screen.Settings -> SettingsScreen(
            onBack = { screen = Screen.SignedIn },
            onAccountDeleted = {
                sessionEmail = ""
                screen = Screen.Login
            },
            onSignedOutEverywhere = {
                sessionEmail = ""
                screen = Screen.Login
            },
        )

        Screen.SignedIn -> HomeScreen(
            onSignOut = {
                AuthSessionStore.clear()
                sessionEmail = ""
                screen = Screen.Login
            },
            onSettings = { screen = Screen.Settings },
        )
    }
}
