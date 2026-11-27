package app.eyemail.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.eyemail.authentication.AuthSessionStore
import app.eyemail.authentication.SessionKeeper
import app.eyemail.home.search.SearchQuery
import app.eyemail.settings.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    /** Which account the state belongs to, so a second sign in never sees the first one. */
    private var loadedFor: String = ""

    init {
        ensureAccount()
    }

    /**
     * Called every time the mailbox screen appears. If the account changed, everything
     * from the previous one is thrown away before anything is loaded.
     */
    fun ensureAccount() {
        val who = AuthSessionStore.uid().orEmpty()
        if (who == loadedFor && loadedFor.isNotBlank()) return
        loadedFor = who
        _state.value = HomeState()
        load()
        loadProfile()
        registerPush()
    }

    /** Push is owned by PushRegistrar, so this only asks it to make sure. */
    fun registerPush() = app.eyemail.push.PushRegistrar.ensureRegistered(
        context = app.eyemail.push.PushRegistrar.appContext(),
        reason = "mailbox",
    )

    /**
     * A live token. Renewed when it is old, so an hour after sign in the app still works.
     */
    private suspend fun token(): String = SessionKeeper.validToken()

    private fun uid(): String = AuthSessionStore.uid().orEmpty()

    /**
     * Loads the folder that is open. The folder is part of the query, so Trash and
     * Spam are real lists and not whatever was left over from the inbox.
     */
    fun load() {
        val folder = _state.value.folder
        _state.update { it.copy(loading = true, error = "") }
        viewModelScope.launch {
            if (token().isBlank() || uid().isBlank()) {
                _state.update { it.copy(error = "Sign in again.", loading = false) }
                return@launch
            }
            try {
                val size = _state.value.pageSize
                val mails = when (folder) {
                    "starred" -> MailRepository.listStarred(uid(), token(), size)
                    // A label can sit on any mail, so this looks at every folder.
                    "labels" -> listOf("inbox", "sent", "drafts", "spam", "trash")
                        .flatMap { each ->
                            runCatching { MailRepository.list(uid(), token(), each, size) }
                                .getOrDefault(emptyList())
                        }
                        .distinctBy { it.id }
                        .sortedByDescending { it.createdAt }
                    "domain" -> emptyList()
                    else -> MailRepository.list(uid(), token(), folder, size)
                }
                _state.update {
                    it.copy(
                        mails = mails,
                        loading = false,
                        searching = false,
                        canLoadMore = mails.size >= size,
                    )
                }
                if (folder == "inbox") Notifier.announce(mails)
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Could not load the mailbox.") }
            }
        }
    }

    /** Empty Trash for real, through the endpoint the web uses. */
    fun emptyTrash() {
        viewModelScope.launch {
            try {
                MailRepository.emptyTrash(token())
                load()
                loadProfile()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Could not empty Trash.") }
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            try {
                val profile = MailRepository.profile(token())
                _state.update {
                    it.copy(
                        storageUsed = profile.optLong("storageUsedBytes", 0),
                        storageLimit = profile.optLong("storageLimitBytes", 1L shl 30),
                    )
                }
            } catch (_: Exception) {
                // The meter is not worth an error line on its own.
            }
        }
    }

    fun openFolder(folder: String) {
        // Picking the Labels row itself means "all labels", not the one picked last time.
        if (folder == "labels") _state.update { it.copy(selectedLabel = null) }
        _state.update {
            it.copy(
                folder = folder,
                openMailId = null,
                openMailCopy = null,
                query = "",
                mails = emptyList(),
                // Ticks made in another folder would act on mail you cannot see.
                selected = emptySet(),
            )
        }
        // The domain screen has no mail; every other row does.
        if (folder != "domain") load()
    }

    /**
     * Search looks at every folder, not only the one on screen. A person looking for an
     * invoice does not know, and should not have to know, whether it is in Sent or Trash.
     */
    private var searchJob: kotlinx.coroutines.Job? = null

    fun search(query: String) {
        _state.update { it.copy(query = query, selected = emptySet()) }
        // Every keystroke used to fire five network calls. One query, one search.
        searchJob?.cancel()
        if (query.isBlank()) {
            _state.update { it.copy(searching = false) }
            load()
            return
        }
        _state.update { it.copy(searching = true, loading = true) }
        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(350)
            try {
                val wanted = SearchQuery.parse(query)
                var failedFolders = 0
                val everywhere = listOf("inbox", "sent", "drafts", "spam", "trash")
                    .flatMap { folder ->
                        runCatching { MailRepository.list(uid(), token(), folder, 50) }
                            .getOrElse {
                                failedFolders += 1
                                emptyList()
                            }
                    }
                    .distinctBy { it.id }
                    .filter { wanted.matches(it) }
                    .sortedByDescending { it.createdAt }
                _state.update {
                    it.copy(
                        mails = everywhere,
                        loading = false,
                        searching = true,
                        canLoadMore = false,
                        error = if (failedFolders > 0) {
                            "Searched ${5 - failedFolders} of 5 folders. The rest did not answer."
                        } else {
                            ""
                        },
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Could not search.") }
            }
        }
    }

    /** One more page of the folder on screen. */
    fun loadMore() {
        val next = _state.value.pageSize + 50
        _state.update { it.copy(pageSize = next) }
        load()
    }

    /* ---------- selection, the same gestures as the web ---------- */

    fun toggleSelect(mailId: String) = _state.update { current ->
        val next = current.selected.toMutableSet()
        if (!next.add(mailId)) next.remove(mailId)
        current.copy(selected = next)
    }

    fun selectAll() = _state.update { it.copy(selected = it.visible.map { mail -> mail.id }.toSet()) }

    fun clearSelection() = _state.update { it.copy(selected = emptySet()) }

    /** One action over everything ticked, so a bulk move is a single pass. */
    fun bulk(action: String) {
        val ids = _state.value.selected.toList()
        if (ids.isEmpty()) return
        val mails = _state.value.mails.filter { it.id in ids }
        _state.update { it.copy(selected = emptySet()) }

        viewModelScope.launch {
            try {
                for (mail in mails) {
                    when (action) {
                        "read" -> {
                            optimistic(mail.id) { it.copy(read = true) }
                            MailRepository.setRead(uid(), token(), mail.id, true)
                        }
                        "unread" -> {
                            optimistic(mail.id) { it.copy(read = false) }
                            MailRepository.setRead(uid(), token(), mail.id, false)
                        }
                        "star" -> {
                            optimistic(mail.id) { it.copy(starred = true) }
                            MailRepository.setStarred(uid(), token(), mail.id, true)
                        }
                        "spam" -> {
                            optimistic(mail.id) { it.copy(folder = "spam") }
                            MailRepository.moveTo(uid(), token(), mail.id, "spam")
                        }
                        "notspam" -> {
                            optimistic(mail.id) { it.copy(folder = "inbox") }
                            MailRepository.moveTo(uid(), token(), mail.id, "inbox")
                        }
                        "trash" -> {
                            optimistic(mail.id) { it.copy(folder = "trash") }
                            MailRepository.moveTo(uid(), token(), mail.id, "trash")
                        }
                        "restore" -> {
                            optimistic(mail.id) { it.copy(folder = "inbox") }
                            MailRepository.moveTo(uid(), token(), mail.id, "inbox")
                        }
                        "forever" -> {
                            _state.update { s -> s.copy(mails = s.mails.filterNot { it.id == mail.id }) }
                            MailRepository.deleteForever(uid(), token(), mail.id)
                        }
                    }
                }
                load()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Could not finish that action.") }
                load()
            }
        }
    }

    /**
     * Put a label on a mail, or take it off. The label names live on the phone, the
     * assignment lives on the mail, so the same word can be used across folders.
     */
    fun toggleLabel(mail: Mail, label: String) {
        val next = if (mail.labels.contains(label)) {
            mail.labels - label
        } else {
            mail.labels + label
        }
        SettingsStore.addLabel(label)
        _state.update { current ->
            current.copy(
                mails = current.mails.map { if (it.id == mail.id) it.copy(labels = next) else it },
                labels = SettingsStore.labels(),
            )
        }
        viewModelScope.launch {
            try {
                MailRepository.setLabels(uid(), token(), mail.id, next)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Could not save the label.") }
                load()
            }
        }
    }

    fun open(mail: Mail) {
        // The copy is what the reader draws, so a refresh cannot pull it out from under
        // the person while they are reading.
        _state.update { it.copy(openMailId = mail.id, openMailCopy = mail) }
        if (!mail.read) setRead(mail, true)
    }

    fun close() = _state.update { it.copy(openMailId = null, openMailCopy = null) }

    fun setRead(mail: Mail, read: Boolean) {
        optimistic(mail.id) { it.copy(read = read) }
        viewModelScope.launch {
            try {
                MailRepository.setRead(uid(), token(), mail.id, read)
            } catch (_: Exception) {
                optimistic(mail.id) { it.copy(read = !read) }
            }
        }
    }

    fun toggleStar(mail: Mail) {
        val next = !mail.starred
        optimistic(mail.id) { it.copy(starred = next) }
        viewModelScope.launch {
            try {
                MailRepository.setStarred(uid(), token(), mail.id, next)
            } catch (_: Exception) {
                optimistic(mail.id) { it.copy(starred = !next) }
            }
        }
    }

    fun move(mail: Mail, folder: String) {
        val previous = mail.folder
        optimistic(mail.id) { it.copy(folder = folder) }
        viewModelScope.launch {
            try {
                MailRepository.moveTo(uid(), token(), mail.id, folder)
            } catch (_: Exception) {
                optimistic(mail.id) { it.copy(folder = previous) }
            }
        }
    }

    /**
     * A label is a name the person keeps, not a property of one mail. It is stored on
     * this phone and shown in the panel; picking one filters the mail that carries it.
     */
    fun createLabel(name: String) {
        if (name.isBlank()) return
        SettingsStore.addLabel(name.trim())
        _state.update { it.copy(labels = SettingsStore.labels()) }
    }

    fun selectLabel(name: String?) {
        _state.update { it.copy(selectedLabel = name) }
    }

    fun send(
        to: String,
        subject: String,
        body: String,
        cc: List<String> = emptyList(),
        onDone: (String?) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                MailRepository.send(token(), to, subject, body, cc)
                load()
                loadProfile()
                onDone(null)
            } catch (e: Exception) {
                onDone(e.message ?: "Could not send the mail.")
            }
        }
    }

    /** A draft that has been sent is no longer a draft. */
    fun deleteDraft(mailId: String, onDone: () -> Unit = {}) {
        if (mailId.isBlank()) {
            onDone()
            return
        }
        viewModelScope.launch {
            runCatching { MailRepository.deleteForever(uid(), token(), mailId) }
            onDone()
        }
    }

    /** Abandoned mail is kept, not thrown away: that is what Drafts is for. */
    fun saveDraft(
        to: String,
        subject: String,
        body: String,
        cc: List<String> = emptyList(),
        onDone: (String?) -> Unit = {},
    ) {
        if (to.isBlank() && subject.isBlank() && body.isBlank()) {
            onDone(null)
            return
        }
        viewModelScope.launch {
            val outcome = runCatching {
                MailRepository.saveDraft(uid(), token(), to, subject, body, cc)
            }
            if (outcome.isSuccess && _state.value.folder == "drafts") load()
            onDone(outcome.exceptionOrNull()?.message)
        }
    }

    private fun optimistic(mailId: String, change: (Mail) -> Mail) {
        _state.update { current ->
            current.copy(
                mails = current.mails.map { if (it.id == mailId) change(it) else it },
                // The reader sees the same change at once, not after a reload.
                openMailCopy = current.openMailCopy?.let { if (it.id == mailId) change(it) else it },
            )
        }
    }
}
