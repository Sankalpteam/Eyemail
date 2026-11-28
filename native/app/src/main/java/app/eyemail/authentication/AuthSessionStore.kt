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
    private const val KEY_REFRESH = "refreshToken"
    private const val KEY_EXPIRES = "expiresAt"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        }
    }

    /** False when the store was never set up, so a caller can tell instead of guessing. */
    fun isReady(): Boolean = prefs != null

    fun save(session: EyemailAuthService.Session) {
        prefs?.edit()
            ?.putString(KEY_EMAIL, session.email)
            ?.putString(KEY_UID, session.uid)
            ?.putString(KEY_TOKEN, session.idToken)
            ?.putString(KEY_REFRESH, session.refreshToken)
            ?.putLong(KEY_EXPIRES, System.currentTimeMillis() + session.expiresInSeconds * 1000)
            ?.apply()
    }

    /** The terms were accepted on this device, so sign in does not ask again. */
    fun termsAgreed(): Boolean = prefs?.getBoolean("termsAgreed", false) ?: false

    fun setTermsAgreed(value: Boolean) {
        prefs?.edit()?.putBoolean("termsAgreed", value)?.apply()
    }

    fun token(): String? = prefs?.getString(KEY_TOKEN, null)

    fun email(): String? = prefs?.getString(KEY_EMAIL, null)

    fun uid(): String? = prefs?.getString(KEY_UID, null)

    fun refreshToken(): String? = prefs?.getString(KEY_REFRESH, null)

    fun expiresAt(): Long = prefs?.getLong(KEY_EXPIRES, 0L) ?: 0L

    /** True when the id token is gone or close to expiring, so it must be renewed first. */
    fun needsRefresh(): Boolean {
        val token = token()
        if (token.isNullOrBlank()) return false
        // Two minutes of room, so a request cannot die on the way.
        return System.currentTimeMillis() > expiresAt() - 120_000
    }

    fun saveRefreshed(idToken: String, refreshToken: String, expiresInSeconds: Long) {
        prefs?.edit()
            ?.putString(KEY_TOKEN, idToken)
            ?.putString(KEY_REFRESH, refreshToken)
            ?.putLong(KEY_EXPIRES, System.currentTimeMillis() + expiresInSeconds * 1000)
            ?.apply()
    }

    fun isSignedIn(): Boolean = !token().isNullOrBlank()

    /** Forget the signed in account. What the device agreed to stays. */
    fun clear() {
        prefs?.edit()
            ?.remove(KEY_EMAIL)
            ?.remove(KEY_UID)
            ?.remove(KEY_TOKEN)
            ?.remove(KEY_REFRESH)
            ?.remove(KEY_EXPIRES)
            ?.apply()
    }
}
