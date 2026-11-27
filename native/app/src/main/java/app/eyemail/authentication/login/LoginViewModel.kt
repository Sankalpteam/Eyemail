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

                AuthSessionStore.save(session)
                _state.update { it.copy(busy = false) }
                _signedIn.value = true
            } catch (e: Exception) {
                _state.update { it.copy(busy = false, error = e.message ?: "Could not sign in.") }
            }
        }
    }
}
