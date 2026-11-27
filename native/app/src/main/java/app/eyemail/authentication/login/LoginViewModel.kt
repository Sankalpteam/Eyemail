package app.eyemail.authentication.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.eyemail.BuildConfig
import app.eyemail.authentication.AuthSessionStore
import app.eyemail.authentication.EyemailAuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _signedIn = MutableStateFlow(false)
    val signedIn: StateFlow<Boolean> = _signedIn.asStateFlow()

    fun onEmail(value: String) = _state.update { it.copy(email = value, error = "") }

    fun onPassword(value: String) = _state.update { it.copy(password = value, error = "") }

    /** The terms box. Remembered on this device, so the next sign in starts ticked. */
    fun onAgree(value: Boolean) {
        AuthSessionStore.setTermsAgreed(value)
        _state.update { it.copy(agreed = value, error = "") }
    }

    init {
        _state.update { it.copy(agreed = AuthSessionStore.termsAgreed()) }
    }

    /** The screen calls this once it has navigated, so the event is used only once. */
    fun consumeSignedIn() {
        _signedIn.value = false
    }

    /** Coming back to this screen always starts from a clean form. */
    fun reset() {
        _state.update { it.copy(password = "", busy = false, error = "") }
    }

    /** Clear the form for a different account, after a refusal. */
    fun startOver() {
        _state.update { LoginState() }
    }

    fun signIn() {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(busy = true, error = "") }

        val typed = current.email.trim().lowercase()
        if (typed.contains("@") && !typed.endsWith("@${BuildConfig.DOMAIN_NAME}")) {
            _state.update {
                it.copy(
                    busy = false,
                    error = "That is not an Eyemail address. Sign in with your @${BuildConfig.DOMAIN_NAME} one.",
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                val address = if (current.email.contains("@")) {
                    current.email.trim().lowercase()
                } else {
                    "${current.email.trim().lowercase()}@${BuildConfig.DOMAIN_NAME}"
                }
                val session = EyemailAuthService.signIn(address, current.password)

                // Signing in proves the password. It does not prove the mailbox exists.
                // Ask the server before letting anyone in, so a login without a mailbox
                // is told here, where they can act, instead of inside an empty inbox.
                val hasMailbox = runCatching { EyemailAuthService.mailboxExists(session.idToken) }
                    .getOrDefault(true)
                if (!hasMailbox) {
                    _state.update {
                        it.copy(
                            busy = false,
                            error = "This sign in has no mailbox behind it. Create the mailbox " +
                                "again, or sign in with another address.",
                        )
                    }
                    return@launch
                }

                if (session.idToken.isBlank() || session.refreshToken.isBlank()) {
                    _state.update {
                        it.copy(
                            busy = false,
                            error = "The sign in did not come back complete. Try again in a moment.",
                        )
                    }
                    return@launch
                }

                if (!AuthSessionStore.isReady()) {
                    _state.update {
                        it.copy(busy = false, error = "The app could not store the sign in. Restart and try again.")
                    }
                    return@launch
                }
                AuthSessionStore.save(session)
                _state.update { it.copy(busy = false) }
                _signedIn.value = true
            } catch (e: Exception) {
                val raw = e.message.orEmpty()
                val readable = when {
                    raw.contains("Unable to resolve host") ||
                        raw.contains("Failed to connect") ||
                        raw.contains("timeout", ignoreCase = true) ||
                        raw.contains("SocketTimeout") ||
                        e is java.net.UnknownHostException ||
                        e is java.net.SocketTimeoutException ||
                        e is java.io.IOException ->
                        "No internet connection. Check your network and try again."
                    raw.isBlank() -> "Could not sign in. Try again in a moment."
                    else -> raw
                }
                _state.update { it.copy(busy = false, error = readable) }
            }
        }
    }
}
