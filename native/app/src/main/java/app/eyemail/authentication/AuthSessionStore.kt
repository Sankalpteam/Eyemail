package app.eyemail.authentication

import android.content.Context
import android.content.SharedPreferences

/**
 * Holds the signed in account. The token lives here and nowhere else, so no
 * screen has to pass it around and none of them can leak it into a log.
 */
object AuthSessionStore {

    private const val FILE = "eyemail_session"
    private const val KEY_EMAIL = "email"
    private const val KEY_UID = "uid"
    private const val KEY_TOKEN = "idToken"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        }
    }

    fun save(session: EyemailAuthService.Session) {
        prefs?.edit()
            ?.putString(KEY_EMAIL, session.email)
            ?.putString(KEY_UID, session.uid)
            ?.putString(KEY_TOKEN, session.idToken)
            ?.apply()
    }

    fun token(): String? = prefs?.getString(KEY_TOKEN, null)

    fun email(): String? = prefs?.getString(KEY_EMAIL, null)

    fun uid(): String? = prefs?.getString(KEY_UID, null)

    fun isSignedIn(): Boolean = !token().isNullOrBlank()

    fun clear() {
        prefs?.edit()?.clear()?.apply()
    }
}
