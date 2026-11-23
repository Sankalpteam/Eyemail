package app.eyemail.authentication.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.eyemail.authentication.AuthSessionStore
import app.eyemail.authentication.EyemailAuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {

    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    private val _signedIn = MutableStateFlow(false)
    val signedIn: StateFlow<Boolean> = _signedIn.asStateFlow()

    /** The screen calls this once it has navigated, so the event fires only once. */
    fun consumeSignedIn() {
        _signedIn.value = false
    }

    /** Coming back always starts at the form with nothing half filled in. */
    fun reset() {
        _state.value = SignUpState()
    }

    fun onName(value: String) = _state.update { it.copy(name = value, error = "") }

    fun onUsername(value: String) = _state.update {
        it.copy(
            username = value.lowercase().filter { c -> c.isLetterOrDigit() || c == '.' || c == '_' },
            error = "",
        )
    }

    fun onRecovery(value: String) = _state.update { it.copy(recovery = value, error = "") }

    fun onPassword(value: String) = _state.update { it.copy(password = value, error = "") }

    fun onOtp(value: String) = _state.update {
        it.copy(otp = value.filter { c -> c.isDigit() }.take(6), error = "")
    }

    fun backToForm() = _state.update { it.copy(step = SignUpState.Step.Form, otp = "", error = "") }

    /** Step one: the server creates the login, then we sign in for a token. */
    fun createAccount() {
        val current = _state.value
        if (!current.canCreate) return
        _state.update { it.copy(busy = true, error = "") }

        viewModelScope.launch {
            try {
                val result = EyemailAuthService.signup(
                    username = current.username.trim(),
                    name = current.name.trim(),
                    recoveryEmail = current.recovery.trim().lowercase(),
                    password = current.password,
                )
                val session = EyemailAuthService.signIn(result.email, current.password)
                lastCodeSentAt = System.currentTimeMillis()
                _state.update {
                    it.copy(
                        busy = false,
                        step = SignUpState.Step.Code,
                        pendingEmail = result.email,
                        pendingMasked = result.maskedRecoveryEmail,
                        pendingToken = session.idToken,
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(busy = false, error = e.message ?: "Could not create the account.") }
            }
        }
    }

    /** Step two: prove the recovery mail, register the mailbox, sign in for real. */
    fun verifyCode() {
        val current = _state.value
        if (!current.canVerify) return
        _state.update { it.copy(busy = true, error = "", note = "") }

        viewModelScope.launch {
            try {
                EyemailAuthService.verifySignupOtp(current.otp, current.pendingToken)
                EyemailAuthService.registerMailbox(
                    username = current.pendingEmail.substringBefore("@"),
                    name = current.name.trim(),
                    recoveryEmail = current.recovery.trim().lowercase(),
                    idToken = current.pendingToken,
                )
                // A fresh sign in, so the session carries a refresh token. Without it the
                // new account would stop working an hour later, with no way to renew.
                val session = runCatching {
                    EyemailAuthService.signIn(current.pendingEmail, current.password)
                }.getOrNull() ?: EyemailAuthService.Session(
                    uid = EyemailAuthService.uidFromToken(current.pendingToken),
                    email = current.pendingEmail,
                    idToken = current.pendingToken,
                )
                AuthSessionStore.save(session)
                _state.update { it.copy(busy = false) }
                _signedIn.value = true
            } catch (e: Exception) {
                _state.update { it.copy(busy = false, error = e.message ?: "That code did not work.") }
            }
        }
    }

    private var lastCodeSentAt: Long = 0L

    fun resendCode() {
        val current = _state.value
        if (current.busy) return
        val waited = (System.currentTimeMillis() - lastCodeSentAt) / 1000
        if (lastCodeSentAt != 0L && waited < 60) {
            _state.update { it.copy(note = "Wait ${60 - waited}s before asking for another code.") }
            return
        }
        _state.update { it.copy(busy = true, error = "", note = "") }

        viewModelScope.launch {
            try {
                EyemailAuthService.sendSignupOtp(current.recovery.trim().lowercase())
                lastCodeSentAt = System.currentTimeMillis()
                _state.update { it.copy(busy = false, note = "A new code is on its way.") }
            } catch (e: Exception) {
                _state.update { it.copy(busy = false, error = e.message ?: "Could not send another code.") }
            }
        }
    }
}
