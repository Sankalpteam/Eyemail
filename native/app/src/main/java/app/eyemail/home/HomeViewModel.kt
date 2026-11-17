package app.eyemail.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.eyemail.authentication.AuthSessionStore
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

    fun load() {
        if (token().isBlank() || uid().isBlank()) {
            _state.update { it.copy(error = "Sign in again.", loading = false) }
            return
        }
        _state.update { it.copy(loading = true, error = "") }
        viewModelScope.launch {
            try {
                val mails = MailRepository.list(uid(), token())
                _state.update { it.copy(mails = mails, loading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Could not load the mailbox.") }
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

    fun openFolder(folder: String) = _state.update {
        it.copy(folder = folder, openMailId = null, query = "")
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

    /** A label is stored on the mail, so creating one writes it to every picked mail. */
    fun createLabel(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                val target = _state.value.mails.firstOrNull { it.folder == "inbox" } ?: return@launch
                val labels = (target.labels + name).distinct()
                MailRepository.setLabels(uid(), token(), target.id, labels)
                load()
            } catch (_: Exception) {
                // A label that failed to save must not look saved.
            }
        }
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
