package app.eyemail

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.eyemail.push.PushRegistrar
import app.eyemail.settings.notifications.NotificationPermission
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * The app side of push.
 *
 * The server sends data only, never a ready made notification. That is deliberate: when
 * the payload carries a notification block, Android draws it itself through a channel it
 * chooses, which on several phones is a channel the person never sees and cannot control,
 * and a message can vanish there with no trace. With data only, this class always draws
 * it, on our own channel, through the same code path that is proven to work in the
 * foreground. One path, no surprises.
 */
class PushService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // Firebase replaced the token. Remember it, then tell the server, with retries.
        app.eyemail.settings.SettingsStore.init(this)
        app.eyemail.settings.SettingsStore.setDeviceToken(token)
        PushRegistrar.ensureRegistered(this, reason = "new token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // When the app is open the system does not draw the notification block, so this
        // is the only copy. When the app is closed the system draws it and this is not
        // called at all. Both paths land on the same channel, so nothing is missed and
        // nothing is doubled.
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "Eyemail"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "New mail"
        show(title, body)
    }

    private fun show(title: String, body: String) {
        // The channel is made again here on purpose: this service can run before the app
        // screen has ever been opened, and a missing channel loses the notification.
        PushRegistrar.ensureChannel(applicationContext)

        if (!NotificationPermission.isGranted()) return

        val open = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            this, 0, open,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val note = NotificationCompat.Builder(this, PushRegistrar.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_mail)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EMAIL)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .setContentIntent(pending)
            .build()

        // One id per message, so a second mail does not replace the first.
        runCatching {
            NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), note)
        }
    }
}
