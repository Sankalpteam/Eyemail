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
                _state.update {
                    it.copy(busy = false, done = "Reset link sent to $masked. Check Inbox and Spam.")
                }
            } catch (e: Exception) {
                _state.update { it.copy(busy = false, error = e.message ?: "Could not send the reset mail.") }
            }
        }
    }
}
