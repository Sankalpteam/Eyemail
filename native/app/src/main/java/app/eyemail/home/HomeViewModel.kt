package app.eyemail.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.eyemail.authentication.AuthSessionStore
import app.eyemail.settings.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        load()
        loadProfile()
    }

    private fun token(): String = AuthSessionStore.token().orEmpty()

    private fun uid(): String = AuthSessionStore.uid().orEmpty()

    /**
     * Loads the folder that is open. The folder is part of the query, so Trash and
     * Spam are real lists and not whatever was left over from the inbox.
     */
    fun load() {
        if (token().isBlank() || uid().isBlank()) {
            _state.update { it.copy(error = "Sign in again.", loading = false) }
            return
        }
        val folder = _state.value.folder
        _state.update { it.copy(loading = true, error = "") }
        viewModelScope.launch {
            try {
                val mails = if (folder == "starred") {
                    MailRepository.listStarred(uid(), token())
                } else {
                    MailRepository.list(uid(), token(), folder)
                }
                _state.update { it.copy(mails = mails, loading = false) }
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
        _state.update { it.copy(folder = folder, openMailId = null, query = "", mails = emptyList()) }
        // Labels and the domain screen are not mail lists, so they do not fetch.
        if (folder != "labels" && folder != "domain") load()
    }

    fun search(query: String) = _state.update { it.copy(query = query) }

    fun open(mail: Mail) {
        _state.update { it.copy(openMailId = mail.id) }
        if (!mail.read) setRead(mail, true)
    }

    fun close() = _state.update { it.copy(openMailId = null) }

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

    fun send(to: String, subject: String, body: String, onDone: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                MailRepository.send(token(), to, subject, body)
                load()
                onDone(null)
            } catch (e: Exception) {
                onDone(e.message ?: "Could not send the mail.")
            }
        }
    }

    private fun optimistic(mailId: String, change: (Mail) -> Mail) {
        _state.update { current ->
            current.copy(mails = current.mails.map { if (it.id == mailId) change(it) else it })
        }
    }
}
