package app.eyemail

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The app side of push. Firebase gives this device a token, we tell the server who it
 * belongs to, and when mail arrives the server sends a message that lands here even
 * when the app is closed.
 */
class PushService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // The token changes over time; the server must always have the current one.
        val uid = app.eyemail.authentication.AuthSessionStore.uid() ?: return
        val idToken = app.eyemail.authentication.AuthSessionStore.token()
        if (idToken.isBlank()) return
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                app.eyemail.home.MailRepository.registerPushToken(uid, idToken, token)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "Eyemail"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "New mail"
        show(title, body)
    }

    private fun show(title: String, body: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel("eyemail_push", "New mail", NotificationManager.IMPORTANCE_DEFAULT)
                    .apply { description = "Told when a new mail arrives" },
            )
        }

        val open = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            this, 0, open,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val note = NotificationCompat.Builder(this, "eyemail_push")
            .setSmallIcon(R.drawable.ic_stat_mail)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        runCatching { manager.notify(System.currentTimeMillis().toInt(), note) }
    }
}
