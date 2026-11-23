package app.eyemail.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import app.eyemail.authentication.AuthSessionStore
import app.eyemail.home.MailRepository
import app.eyemail.settings.SettingsStore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * One place that owns push, so a notification can never stop for a reason nobody can see.
 *
 * The rules this file enforces, and why each one exists:
 *
 * 1. The notification channel is created at app start, before any message can arrive.
 *    A message that arrives before its channel exists is dropped by Android, silently.
 *
 * 2. There is exactly one channel id for mail, [CHANNEL_ID]. Two ids meant a person could
 *    turn one off in Android settings and still see the other, which looks like a bug.
 *
 * 3. The device token is remembered on disk and offered to the server again on every app
 *    start, every return to the app, and whenever Firebase replaces it. A token that is
 *    only sent once is lost the first time that one call fails.
 *
 * 4. A failed registration is retried three times with a growing pause, and the reason is
 *    written down, so Settings can say what went wrong instead of showing a silent nothing.
 */
object PushRegistrar {

    const val CHANNEL_ID = "eyemail_mail"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var inFlight: Job? = null
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        SettingsStore.init(context)
        ensureChannel(context.applicationContext)
    }

    /**
     * Signing out must also stop the notifications. Otherwise the phone keeps buzzing for
     * an account nobody is signed in to, which is the worst kind of leak.
     */
    fun unregister(context: Context, onDone: () -> Unit = {}) {
        SettingsStore.init(context)
        val deviceToken = SettingsStore.deviceToken()
        val idToken = AuthSessionStore.token().orEmpty()
        if (deviceToken.isBlank() || idToken.isBlank()) {
            SettingsStore.setPushState(false, "signed out")
            onDone()
            return
        }
        scope.launch {
            runCatching { MailRepository.fcmUnsubscribe(idToken, deviceToken) }
            SettingsStore.setDeviceToken("")
            SettingsStore.setPushState(false, "signed out")
            onDone()
        }
    }

    /** The context kept at init, so callers without one can still ask for a retry. */
    fun appContext(): Context = appContext ?: throw IllegalStateException("PushRegistrar.init was not called")

    /** The channel must exist before the first message, so this runs at app start. */
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "New mail",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Told when a new mail arrives in the inbox"
            enableVibration(true)
            setShowBadge(true)
        }
        manager.createNotificationChannel(channel)

        // The old second channel is removed, so nothing can be quietly off there.
        runCatching { manager.deleteNotificationChannel("eyemail_push") }
    }

    /**
     * Make sure the server knows this phone. Safe to call as often as you like: it is
     * idempotent and it never runs twice at the same time.
     */
    fun ensureRegistered(context: Context, reason: String = "app") {
        SettingsStore.init(context)
        if (inFlight?.isActive == true) return

        inFlight = scope.launch {
            // A live token: an expired one would make registration fail for no visible reason.
            val idToken = app.eyemail.authentication.SessionKeeper.validToken()
            val uid = AuthSessionStore.uid().orEmpty()
            if (idToken.isBlank() || uid.isBlank()) {
                SettingsStore.setPushState(false, "not signed in")
                return@launch
            }
            var lastError = ""
            repeat(3) { attempt ->
                val freshId = app.eyemail.authentication.SessionKeeper.validToken()
                val token = runCatching { FirebaseMessaging.getInstance().token.awaitToken() }
                    .getOrElse { error ->
                        lastError = "firebase: ${error.message ?: "no token"}"
                        null
                    }
                if (token != null) {
                    SettingsStore.setDeviceToken(token)
                    val outcome = runCatching {
                        MailRepository.registerPushToken(uid, freshId, token)
                    }
                    if (outcome.isSuccess) {
                        SettingsStore.setPushState(true, reason)
                        return@launch
                    }
                    lastError = outcome.exceptionOrNull()?.message.orEmpty()
                }
                delay(1500L * (attempt + 1))
            }
            SettingsStore.setPushState(false, lastError.ifBlank { "unknown" })
        }
    }
}

/** Firebase returns a Task; this turns it into a suspend call without extra libraries. */
private suspend fun com.google.android.gms.tasks.Task<String>.awaitToken(): String {
    while (!isComplete) delay(50)
    return if (isSuccessful) result else throw (exception ?: IllegalStateException("no token"))
}
