package in.eyemail.app.settings

import android.content.Context
import android.content.SharedPreferences

/**
 * The choices that belong to this phone, not to the account: theme, density,
 * signature, undo delay. Same idea as the web app's localStorage, so a setting
 * saved here is still there after a restart.
 */
object SettingsStore {

    private const val FILE = "eyemail_settings"
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        }
    }

    fun theme(): String = prefs?.getString("theme", "light") ?: "light"
    fun setTheme(value: String) = prefs?.edit()?.putString("theme", value)?.apply()

    fun density(): String = prefs?.getString("density", "comfortable") ?: "comfortable"
    fun setDensity(value: String) = prefs?.edit()?.putString("density", value)?.apply()

    fun displayName(): String = prefs?.getString("displayName", "") ?: ""
    fun setDisplayName(value: String) = prefs?.edit()?.putString("displayName", value)?.apply()

    fun signature(): String = prefs?.getString("signature", "") ?: ""
    fun setSignature(value: String) = prefs?.edit()?.putString("signature", value)?.apply()

    fun undoSeconds(): Int = prefs?.getInt("undoSeconds", 5) ?: 5
    fun setUndoSeconds(value: Int) = prefs?.edit()?.putInt("undoSeconds", value)?.apply()

    fun confirmDelete(): Boolean = prefs?.getBoolean("confirmDelete", true) ?: true
    fun setConfirmDelete(value: Boolean) = prefs?.edit()?.putBoolean("confirmDelete", value)?.apply()

    fun notifications(): Boolean = prefs?.getBoolean("notifications", false) ?: false
    fun setNotifications(value: Boolean) = prefs?.edit()?.putBoolean("notifications", value)?.apply()
}
