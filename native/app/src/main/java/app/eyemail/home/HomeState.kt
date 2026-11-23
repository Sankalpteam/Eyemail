package app.eyemail.home

/** Which folder is open, what is being searched, and what came back. */
data class HomeState(
    val folder: String = "inbox",
    val mails: List<Mail> = emptyList(),
    val query: String = "",
    val openMailId: String? = null,
    val openMailCopy: Mail? = null,
    val loading: Boolean = false,
    val error: String = "",
    val storageUsed: Long = 0,
    val storageLimit: Long = 1L shl 30,
    val labels: List<String> = emptyList(),
    val selectedLabel: String? = null,
    val selected: Set<String> = emptySet(),
    val pageSize: Int = 50,
    val canLoadMore: Boolean = false,
    val searching: Boolean = false,
    val pushRegistered: Boolean = false,
) {
    val selecting: Boolean get() = selected.isNotEmpty()

    val visible: List<Mail>
        get() {
            val base = mails.filter { mail ->
                when (folder) {
                    "starred" -> mail.starred && mail.folder != "trash"
                    "labels" -> mail.folder != "trash" && mail.labels.isNotEmpty() &&
                        (selectedLabel == null || mail.labels.contains(selectedLabel))
                    else -> mail.folder == folder
                }
            }
            if (query.isBlank()) return base
            val parsed = app.eyemail.home.search.SearchQuery.parse(query)
            return base.filter(parsed::matches)
        }

    val openMail: Mail?
        get() = openMailCopy ?: mails.firstOrNull { it.id == openMailId }

    val unreadCount: Int
        get() = mails.count { it.folder == "inbox" && !it.read }

    val storageFull: Boolean
        get() = storageLimit > 0 && storageUsed >= storageLimit
}
