package app.eyemail.home

import app.eyemail.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** One mail, as the app needs it. Firestore speaks in typed values; this is the plain shape. */
data class Mail(
    val id: String,
    val from: String,
    val fromName: String,
    val to: String,
    val subject: String,
    val snippet: String,
    val body: String,
    val folder: String,
    val read: Boolean,
    val starred: Boolean,
    val createdAt: String,
    val labels: List<String>,
)

/**
 * Firestore over REST, plus the two backend calls the mailbox needs.
 *
 * The web app talks to Firestore directly with the browser SDK; here the same
 * documents are read and written with the same rules, using the signed in token.
 */
object MailRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json; charset=utf-8".toMediaType()

    class ApiError(message: String) : Exception(message)

    private fun docsUrl(uid: String, tail: String = ""): String {
        val project = BuildConfig.FIRESTORE_PROJECT
        val base = "https://firestore.googleapis.com/v1/projects/$project/databases/(default)/documents"
        return "$base/users/$uid/emails$tail"
    }

    private fun value(field: JSONObject?, name: String): String =
        field?.optJSONObject(name)?.optString("stringValue").orEmpty()

    private fun boolValue(field: JSONObject?, name: String): Boolean =
        field?.optJSONObject(name)?.optBoolean("booleanValue", false) ?: false

    private fun labels(field: JSONObject?): List<String> {
        val arr = field?.optJSONObject("labels")?.optJSONObject("arrayValue")?.optJSONArray("values")
            ?: return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            arr.optJSONObject(i)?.optString("stringValue")?.takeIf { it.isNotBlank() }
        }
    }

    private fun toMail(doc: JSONObject): Mail {
        val fields = doc.optJSONObject("fields")
        val name = doc.optString("name")
        return Mail(
            id = name.substringAfterLast('/'),
            from = value(fields, "from"),
            fromName = value(fields, "fromName").ifBlank { value(fields, "from") },
            to = value(fields, "to"),
            subject = value(fields, "subject").ifBlank { "(No Subject)" },
            snippet = value(fields, "snippet"),
            body = value(fields, "body"),
            folder = value(fields, "folder").ifBlank { "inbox" },
            read = boolValue(fields, "read"),
            starred = boolValue(fields, "starred"),
            createdAt = value(fields, "createdAt"),
            labels = labels(fields),
        )
    }

    private suspend fun get(url: String, token: String): JSONObject = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).header("Authorization", "Bearer $token").get().build()
        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw ApiError(readError(raw))
            JSONObject(raw)
        }
    }

    private suspend fun patch(url: String, token: String, fields: JSONObject): Unit = withContext(Dispatchers.IO) {
        val body = JSONObject().put("fields", fields).toString().toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .patch(body)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ApiError(readError(response.body?.string().orEmpty()))
        }
    }

    private suspend fun delete(url: String, token: String) = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).header("Authorization", "Bearer $token").delete().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ApiError(readError(response.body?.string().orEmpty()))
        }
    }

    private fun readError(raw: String): String = runCatching {
        JSONObject(raw).optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() }
    }.getOrNull() ?: "Could not reach the mailbox."

    /** The newest page of one folder. Firestore returns them unsorted, so we sort here. */
    suspend fun list(uid: String, token: String, pageSize: Int = 50): List<Mail> {
        val url = docsUrl(uid) + "?pageSize=$pageSize&orderBy=" +
            java.net.URLEncoder.encode("createdAt desc", "UTF-8")
        val json = get(url, token)
        val docs = json.optJSONArray("documents") ?: JSONArray()
        return (0 until docs.length()).map { toMail(docs.getJSONObject(it)) }
    }

    suspend fun setRead(uid: String, token: String, mailId: String, read: Boolean) {
        patch(docsUrl(uid, "/$mailId") + "?updateMask.fieldPaths=read", token,
            JSONObject().put("read", JSONObject().put("booleanValue", read)))
    }

    suspend fun setStarred(uid: String, token: String, mailId: String, starred: Boolean) {
        patch(docsUrl(uid, "/$mailId") + "?updateMask.fieldPaths=starred", token,
            JSONObject().put("starred", JSONObject().put("booleanValue", starred)))
    }

    suspend fun setLabels(uid: String, token: String, mailId: String, labels: List<String>) {
        val values = org.json.JSONArray()
        labels.forEach { values.put(JSONObject().put("stringValue", it)) }
        patch(
            docsUrl(uid, "/$mailId") + "?updateMask.fieldPaths=labels",
            token,
            JSONObject().put(
                "labels",
                JSONObject().put("arrayValue", JSONObject().put("values", values)),
            ),
        )
    }

    suspend fun moveTo(uid: String, token: String, mailId: String, folder: String) {
        patch(docsUrl(uid, "/$mailId") + "?updateMask.fieldPaths=folder", token,
            JSONObject().put("folder", JSONObject().put("stringValue", folder)))
    }

    suspend fun deleteForever(uid: String, token: String, mailId: String) {
        delete(docsUrl(uid, "/$mailId"), token)
    }

    /** Send through the backend, the same endpoint the web composer uses. */
    suspend fun send(token: String, to: String, subject: String, body: String): Unit =
        withContext(Dispatchers.IO) {
            val payload = JSONObject()
                .put("to", to)
                .put("subject", subject)
                .put("body", body)
            val request = Request.Builder()
                .url("${BuildConfig.BACKEND_URL}/api/send-email")
                .header("Authorization", "Bearer $token")
                .post(payload.toString().toRequestBody(JSON))
                .build()
            client.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val detail = runCatching { JSONObject(raw).optString("detail") }.getOrNull()
                    throw ApiError(detail?.takeIf { it.isNotBlank() } ?: "Could not send the mail.")
                }
            }
        }

    /** Storage and the free plan limit, from the profile the backend already serves. */
    suspend fun profile(token: String): JSONObject = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${BuildConfig.BACKEND_URL}/api/user/profile")
            .header("Authorization", "Bearer $token")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw ApiError(readError(raw))
            JSONObject(raw).optJSONObject("profile") ?: JSONObject()
        }
    }
}
