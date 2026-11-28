package app.eyemail.authentication.forgot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.eyemail.BuildConfig
import app.eyemail.authentication.EyemailAuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    /** Coming back always starts clean. */
    fun reset() {
        _state.value = ForgotPasswordState()
    }

    /** The address typed on the sign in screen, so it is not asked twice. */
    fun prefill(value: String) {
        val clean = value.trim()
        if (clean.isBlank()) return
        _state.update { it.copy(email = clean) }
    }

    /** Clear the sent message so the button works again, on purpose. */
    fun allowResend() = _state.update { it.copy(done = "", error = "") }

    fun onEmail(value: String) = _state.update { it.copy(email = value, error = "", done = "") }

    fun send() {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(busy = true, error = "", done = "") }

        viewModelScope.launch {
            try {
                val address = if (current.email.contains("@")) {
                    current.email.trim().lowercase()
                } else {
                    "${current.email.trim().lowercase()}@${BuildConfig.DOMAIN_NAME}"
                }
                val masked = EyemailAuthService.forgotPassword(address)
                val message = if (masked.isBlank()) {
                    "If $address exists, a reset link has been sent to the recovery email on that account."
                } else {
                    "Reset link sent to $masked. Check Inbox and Spam."
                }
                _state.update { it.copy(busy = false, done = message) }
            } catch (e: Exception) {
                _state.update { it.copy(busy = false, error = e.message ?: "Could not send the reset mail.") }
            }
        }
    }
}
