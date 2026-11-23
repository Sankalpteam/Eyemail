package app.eyemail.home

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.eyemail.R
import app.eyemail.settings.SettingsStore
import app.eyemail.settings.notifications.NotificationPermission

/**
 * Posts one notification per new inbox mail, the way the web does with the browser.
 * It stays silent when the person turned the setting off or the phone refused.
 */
object Notifier {

    private const val CHANNEL_ID = app.eyemail.push.PushRegistrar.CHANNEL_ID
    private var appContext: Context? = null
    private val seen = mutableSetOf<String>()
    private var firstLoad = true

    fun init(context: Context) {
        appContext = context.applicationContext
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val context = appContext ?: return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "New mail",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = "Told when a new mail arrives in the inbox" }
        manager.createNotificationChannel(channel)
    }

    /** Call with the inbox list after every load. */
    fun announce(mails: List<Mail>) {
        val fresh = mails.filter { !it.read && !seen.contains(it.id) }
        mails.forEach { seen.add(it.id) }

        // The first load of a session is history, not news.
        if (firstLoad) {
            firstLoad = false
            return
        }
        if (!SettingsStore.notifications() || !NotificationPermission.isGranted()) return

        val context = appContext ?: return
        val manager = NotificationManagerCompat.from(context)
        fresh.take(3).forEach { mail ->
            val note = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_mail)
                .setContentTitle(mail.fromName.ifBlank { mail.from })
                .setContentText(mail.subject)
                .setStyle(NotificationCompat.BigTextStyle().bigText(mail.snippet.ifBlank { mail.subject }))
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .build()
            runCatching { manager.notify(mail.id.hashCode(), note) }
        }
    }
}
