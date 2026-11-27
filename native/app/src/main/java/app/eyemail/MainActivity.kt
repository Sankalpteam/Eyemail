package app.eyemail

import android.os.Bundle
import android.view.WindowManager
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.launch
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
        // Nothing is held back: the moment the first frame is ready, Android swaps its own
        // launch screen for ours, so the person never sees an empty screen in between.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        AuthSessionStore.init(this)
        SettingsStore.init(this)
        ThemeState.load()
        app.eyemail.settings.notifications.NotificationPermission.init(this)
        app.eyemail.home.Notifier.init(this)
        app.eyemail.push.PushRegistrar.init(this)

        // Push is offered to the server at start and on every return to the app. The
        // registrar is idempotent and retries, so one failure is never the last word.
        app.eyemail.push.PushRegistrar.ensureRegistered(this, reason = "start")
        lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
            override fun onStart(owner: androidx.lifecycle.LifecycleOwner) {
                app.eyemail.push.PushRegistrar.ensureRegistered(this@MainActivity, reason = "resume")
            }
        })
        // No screenshots and no recents thumbnail while the person is signed out, so a
        // password on screen cannot end up in the gallery or the task switcher. The flag is
        // lifted as soon as there is a session, because mail should still be shareable.
        val guard = WindowManager.LayoutParams.FLAG_SECURE
        if (AuthSessionStore.token().isNullOrBlank()) {
            window.addFlags(guard)
        }
        lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
            override fun onResume(owner: androidx.lifecycle.LifecycleOwner) {
                val signedOut = AuthSessionStore.token().isNullOrBlank()
                if (signedOut) window.addFlags(guard) else window.clearFlags(guard)
            }
        })

        enableEdgeToEdge()
        setContent {
            EyemailTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Ey.Bg) {
                    // The system splash can only show the icon, so the branded one is drawn
                    // here for a moment: mark, product name, and the company line.
                    var showSplash by androidx.compose.runtime.remember {
                        androidx.compose.runtime.mutableStateOf(true)
                    }
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        // Long enough to read the name and the company line, short enough
                        // that nobody waits. It is the only splash they see, because the
                        // system one is drawn blank.
                        kotlinx.coroutines.delay(750)
                        showSplash = false
                    }

                    if (showSplash) {
                        app.eyemail.splash.SplashScreen()
                    } else {
                        EyemailApp()
                    }
                }
            }
        }
    }
}

private enum class Screen { Login, SignUp, Forgot, SignedIn, Settings }

@Composable
private fun EyemailApp() {
    val appContext = androidx.compose.ui.platform.LocalContext.current.applicationContext
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
                app.eyemail.push.PushRegistrar.ensureRegistered(
                    appContext,
                    reason = "signed in",
                )
                screen = Screen.SignedIn
            },
        )

        Screen.SignUp -> SignUpScreen(
            onSignIn = { screen = Screen.Login },
            onSignedIn = {
                sessionEmail = AuthSessionStore.email().orEmpty()
                app.eyemail.push.PushRegistrar.ensureRegistered(
                    appContext,
                    reason = "signed in",
                )
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
                // Stop the notifications first: they belong to the account being left.
                // The unsubscribe needs the token, so the session is cleared after it.
                app.eyemail.push.PushRegistrar.unregister(appContext)
                AuthSessionStore.clear()
                sessionEmail = ""
                screen = Screen.Login
            },
            onSettings = { screen = Screen.Settings },
        )
    }
}
