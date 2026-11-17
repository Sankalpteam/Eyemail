package app.eyemail.home.search

import app.eyemail.home.Mail

/** The same query words the web search accepts: from:, is:unread, is:starred, and plain text. */
data class SearchQuery(
    val text: String = "",
    val from: String? = null,
    val unreadOnly: Boolean = false,
    val starredOnly: Boolean = false,
) {
    val isEmpty: Boolean
        get() = text.isBlank() && from == null && !unreadOnly && !starredOnly

    fun matches(mail: Mail): Boolean {
        if (unreadOnly && mail.read) return false
        if (starredOnly && !mail.starred) return false
        if (from != null) {
            val hay = (mail.fromName + " " + mail.from).lowercase()
            if (!hay.contains(from)) return false
        }
        if (text.isNotBlank()) {
            val hay = (mail.subject + " " + mail.snippet + " " + mail.body + " " + mail.fromName).lowercase()
            if (!hay.contains(text.lowercase())) return false
        }
        return true
    }

    companion object {
        fun parse(raw: String): SearchQuery {
            var text = ""
            var from: String? = null
            var unread = false
            var starred = false
            raw.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.forEach { token ->
                val lower = token.lowercase()
                when {
                    lower.startsWith("from:") -> from = lower.removePrefix("from:")
                    lower == "is:unread" -> unread = true
                    lower == "is:starred" -> starred = true
                    else -> text = (text + " " + token).trim()
                }
            }
            return SearchQuery(text = text, from = from, unreadOnly = unread, starredOnly = starred)
        }
    }
}
