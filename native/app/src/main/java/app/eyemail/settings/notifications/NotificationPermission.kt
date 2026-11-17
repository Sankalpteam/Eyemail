package app.eyemail.settings.notifications

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

/**
 * Whether this phone will let us post notifications at all. Android 13 asks at
 * runtime; below that the answer is simply whether the person turned them off.
 */
object NotificationPermission {

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun isGranted(): Boolean {
        val context = appContext ?: return false
        return NotificationManagerCompat.from(context).areNotificationsEnabled() &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED)
    }
}
