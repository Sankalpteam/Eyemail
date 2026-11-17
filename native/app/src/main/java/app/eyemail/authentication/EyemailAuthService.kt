package app.eyemail.authentication

import app.eyemail.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Every call the three auth screens make, in one place.
 *
 * The endpoints are the same ones the web app calls, with the same bodies, so a
 * change on the server never needs a second implementation here.
 */
object EyemailAuthService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json; charset=utf-8".toMediaType()

    class ApiError(message: String) : Exception(message)

    data class SignupResult(val email: String, val maskedRecoveryEmail: String)
    data class Session(val uid: String, val email: String, val idToken: String)

    private suspend fun post(
        path: String,
        body: JSONObject,
        idToken: String? = null,
        absolute: Boolean = false,
    ): JSONObject = withContext(Dispatchers.IO) {
        val url = if (absolute) path else "${BuildConfig.BACKEND_URL}$path"
        val builder = Request.Builder()
            .url(url)
            .post(body.toString().toRequestBody(JSON))
            .header("Content-Type", "application/json")
        if (idToken != null) builder.header("Authorization", "Bearer $idToken")

        client.newCall(builder.build()).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            val json = if (raw.isBlank()) JSONObject() else runCatching { JSONObject(raw) }.getOrElse { JSONObject() }
            if (!response.isSuccessful) {
                val detail = readError(json)
                // Configuration failures are hard to tell apart on a phone, so the
                // message carries the facts: the code, and how long the key is.
                val hint = if (detail.contains("unregistered callers") || detail.contains("API key")) {
                    " (HTTP ${response.code}, key length ${BuildConfig.FIREBASE_API_KEY.trim().length})"
                } else {
                    ""
                }
                throw ApiError(friendly(detail) + hint)
            }
            json
        }
    }

    /** Reads the message out of either shape: {"detail":"..."} or {"error":{"message":"..."}}. */
    private fun readError(json: JSONObject): String {
        json.optString("detail").takeIf { it.isNotBlank() }?.let { return it }
        val nested = json.optJSONObject("error")
        if (nested != null) {
            nested.optString("message").takeIf { it.isNotBlank() }?.let { return it }
            return "Something went wrong."
        }
        return json.optString("error").ifBlank { "Something went wrong." }
    }

    /** The server speaks in sentences already; this only softens the raw Firebase codes. */
    private fun friendly(detail: String): String = when {
        detail.contains("EMAIL_EXISTS") -> "That address is already taken."
        detail.contains("WEAK_PASSWORD") -> "That password is too short. Use at least 6 characters."
        detail.contains("EMAIL_NOT_FOUND") -> "No account with that address."
        detail.contains("INVALID_PASSWORD") -> "That password is not right."
        detail.contains("TOO_MANY_ATTEMPTS_TRY_LATER") -> "Too many tries. Wait a minute and try again."
        detail.contains("INVALID_LOGIN_CREDENTIALS") -> "That address or password is not right."
        detail.contains("INVALID_EMAIL") -> "That address does not look right."
        detail.contains("unregistered callers") ->
            "This build is not allowed to talk to Firebase. The FIREBASE_API_KEY secret is missing or wrong."
        detail.contains("API key not valid") || detail.contains("API_KEY_INVALID") ->
            "The Firebase key in this build is not valid. Check the FIREBASE_API_KEY secret."
        detail.contains("PERMISSION_DENIED") || detail.contains("API has not been used") ->
            "Firebase refused the call. The Identity Toolkit API may be off for this project."
        detail.contains("service account") || detail.contains("Could not load the default credentials") ->
            "The server has no Firebase credentials. Check FIREBASE_SERVICE_ACCOUNT_JSON on Render."
        else -> detail
    }

    /** Firebase sign-in over REST, so the app needs no google-services.json yet. */
    suspend fun signIn(email: String, password: String): Session {
        val body = JSONObject()
            .put("email", email)
            .put("password", password)
            .put("returnSecureToken", true)
        val key = BuildConfig.FIREBASE_API_KEY.trim()
        if (key.isBlank()) {
            throw ApiError(
                "This build has no Firebase key at all. Set FIREBASE_API_KEY in the repository " +
                    "secrets and rebuild.",
            )
        }
        val json = post(
            "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$key",
            body,
            absolute = true,
        )
        return Session(
            uid = json.optString("localId"),
            email = json.optString("email", email),
            idToken = json.optString("idToken"),
        )
    }

    suspend fun checkUsername(username: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${BuildConfig.BACKEND_URL}/api/check-username/$username")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            val json = runCatching { JSONObject(raw) }.getOrDefault(JSONObject())
            json.optBoolean("available", false)
        }
    }

    suspend fun signup(username: String, name: String, recoveryEmail: String, password: String): SignupResult {
        val body = JSONObject()
            .put("username", username)
            .put("name", name)
            .put("recovery_email", recoveryEmail)
            .put("password", password)
        val json = post("/api/auth/signup", body)
        return SignupResult(
            email = json.optString("email", "$username@${BuildConfig.DOMAIN_NAME}"),
            maskedRecoveryEmail = json.optString("maskedRecoveryEmail", recoveryEmail),
        )
    }

    suspend fun verifySignupOtp(otp: String, idToken: String) {
        post("/api/auth/verify-signup-otp", JSONObject().put("otp", otp), idToken)
    }

    suspend fun sendSignupOtp(recoveryEmail: String) {
        post("/api/auth/send-signup-otp", JSONObject().put("recovery_email", recoveryEmail))
    }

    suspend fun registerMailbox(username: String, name: String, recoveryEmail: String, idToken: String) {
        val body = JSONObject()
            .put("username", username)
            .put("name", name)
            .put("recovery_email", recoveryEmail)
        post("/api/auth/register-mailbox", body, idToken)
    }

    /** The uid is inside the token already, so no second call is needed to read it. */
    fun uidFromToken(idToken: String): String {
        val payload = idToken.split(".").getOrNull(1) ?: return ""
        val json = String(android.util.Base64.decode(payload, android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING))
        return runCatching { JSONObject(json).optString("user_id") }.getOrDefault("")
    }

    /** Ends every session; the web calls this from Sign out of all devices. */
    suspend fun signOutAll(idToken: String) {
        post("/api/auth/sign-out-all", JSONObject(), idToken)
    }

    /** Removes the mailbox, its mail and the login. Not reversible. */
    suspend fun deleteAccount(idToken: String) {
        post("/api/auth/delete-account", JSONObject(), idToken)
    }

    suspend fun forgotPassword(eyemail: String): String {
        val json = post("/api/auth/forgot-password", JSONObject().put("eyemail", eyemail))
        return json.optString("maskedRecoveryEmail")
    }
}
