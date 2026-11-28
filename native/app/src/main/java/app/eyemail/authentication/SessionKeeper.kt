package app.eyemail.authentication

import app.eyemail.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Keeps the session alive.
 *
 * A Firebase id token dies after an hour. Before this file existed the app stored one
 * token and used it forever, so after an hour every read and write failed with 401 and
 * the only way out was signing in again. That is a bug that appears an hour after the
 * person says everything works, which is the worst kind.
 *
 * Now the token is renewed before it expires, once, even if several screens ask at the
 * same moment, because the renewal is behind a mutex.
 */
object SessionKeeper {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json; charset=utf-8".toMediaType()
    private val mutex = Mutex()

    @Volatile
    private var lastError: String = ""

    fun error(): String = lastError

    /** The token to use right now. Renews it first when it is old. */
    suspend fun validToken(): String = mutex.withLock {
        val current = AuthSessionStore.token().orEmpty()
        if (current.isBlank()) return@withLock ""
        if (!AuthSessionStore.needsRefresh()) return@withLock current

        val refresh = AuthSessionStore.refreshToken().orEmpty()
        if (refresh.isBlank()) {
            // Signed in by an older build that did not keep a refresh token: the only
            // honest answer is the old token, and the caller will surface the 401.
            return@withLock current
        }

        val renewed = runCatching { exchange(refresh) }.getOrElse { error ->
            lastError = error.message ?: "could not renew the session"
            null
        }
        if (renewed == null) return@withLock current

        AuthSessionStore.saveRefreshed(renewed.first, renewed.second, renewed.third)
        lastError = ""
        AuthSessionStore.token().orEmpty()
    }

    /** securetoken is the one endpoint that turns a refresh token into a new id token. */
    private suspend fun exchange(refreshToken: String): Triple<String, String, Long> =
        withContext(Dispatchers.IO) {
            val key = BuildConfig.FIREBASE_API_KEY.trim()
            require(key.isNotBlank()) { "no Firebase key in this build" }

            val encoded = java.net.URLEncoder.encode(refreshToken, "UTF-8")
            val body = "grant_type=refresh_token&refresh_token=$encoded"
                .toRequestBody("application/x-www-form-urlencoded".toMediaType())
            val request = Request.Builder()
                .url("https://securetoken.googleapis.com/v1/token?key=$key")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val detail = runCatching { JSONObject(raw).optString("error_description") }
                        .getOrNull()
                        ?.takeIf { it.isNotBlank() }
                    // The server refused the refresh token itself, which means it is dead:
                    // forget the session so the app asks for a sign in instead of failing
                    // every request for ever.
                    if (response.code == 400 || response.code == 401) {
                        AuthSessionStore.clear()
                    }
                    throw IllegalStateException(detail ?: "session renewal failed (HTTP ${response.code})")
                }
                val json = JSONObject(raw)
                val idToken = json.optString("id_token")
                val newRefresh = json.optString("refresh_token").ifBlank { refreshToken }
                val expires = json.optString("expires_in").toLongOrNull() ?: 3600L
                if (idToken.isBlank()) throw IllegalStateException("session renewal returned no token")
                Triple(idToken, newRefresh, expires)
            }
        }
}
