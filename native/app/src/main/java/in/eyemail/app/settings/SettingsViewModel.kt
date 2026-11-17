package in.eyemail.app.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import in.eyemail.app.BuildConfig
import in.eyemail.app.authentication.AuthSessionStore
import in.eyemail.app.authentication.EyemailAuthService
import in.eyemail.app.home.MailRepository
import in.eyemail.app.settings.about.AboutState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        // Everything this phone remembers, read once when the screen opens.
        _state.update {
            it.copy(
                account = it.account.copy(
                    displayName = SettingsStore.displayName(),
                    email = AuthSessionStore.email().orEmpty(),
                ),
                appearance = it.appearance.copy(
                    theme = SettingsStore.theme(),
                    density = SettingsStore.density(),
                ),
                mail = it.mail.copy(
                    signature = SettingsStore.signature(),
                    undoSeconds = SettingsStore.undoSeconds(),
                    confirmDelete = SettingsStore.confirmDelete(),
                ),
                notifications = it.notifications.copy(enabled = SettingsStore.notifications()),
                about = AboutState(version = "1.0", domain = BuildConfig.DOMAIN_NAME),
            )
        }
        loadProfile()
    }

    fun select(section: SettingsState.Section) = _state.update { it.copy(section = section) }

    private fun loadProfile() {
        _state.update { it.copy(loading = true, error = "") }
        viewModelScope.launch {
            try {
                val profile = MailRepository.profile(AuthSessionStore.token().orEmpty())
                _state.update {
                    it.copy(
                        loading = false,
                        account = it.account.copy(
                            recoveryEmail = profile.optString("recoveryEmail"),
                            displayName = it.account.displayName.ifBlank { profile.optString("name") },
                        ),
                        storage = it.storage.copy(
                            usedBytes = profile.optLong("storageUsedBytes", 0),
                            limitBytes = profile.optLong("storageLimitBytes", 1L shl 30),
                        ),
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Could not load the profile.") }
            }
        }
    }

    fun setName(value: String) {
        SettingsStore.setDisplayName(value)
        _state.update { it.copy(account = it.account.copy(displayName = value)) }
    }

    fun setTheme(value: String) {
        SettingsStore.setTheme(value)
        _state.update { it.copy(appearance = it.appearance.copy(theme = value)) }
    }

    fun setDensity(value: String) {
        SettingsStore.setDensity(value)
        _state.update { it.copy(appearance = it.appearance.copy(density = value)) }
    }

    fun setSignature(value: String) {
        SettingsStore.setSignature(value)
        _state.update { it.copy(mail = it.mail.copy(signature = value)) }
    }

    fun setUndo(seconds: Int) {
        SettingsStore.setUndoSeconds(seconds)
        _state.update { it.copy(mail = it.mail.copy(undoSeconds = seconds)) }
    }

    fun setConfirmDelete(value: Boolean) {
        SettingsStore.setConfirmDelete(value)
        _state.update { it.copy(mail = it.mail.copy(confirmDelete = value)) }
    }

    fun setNotifications(enabled: Boolean, granted: Boolean, blocked: Boolean) {
        SettingsStore.setNotifications(enabled)
        _state.update {
            it.copy(notifications = it.notifications.copy(enabled = enabled, permissionGranted = granted, blocked = blocked))
        }
    }

    /** Password is never set here: the server mails a link to the recovery address. */
    fun sendResetLink() {
        _state.update { it.copy(account = it.account.copy(sendingReset = true, resetNote = "", resetError = "")) }
        viewModelScope.launch {
            try {
                val masked = EyemailAuthService.forgotPassword(_state.value.account.email)
                _state.update {
                    it.copy(
                        account = it.account.copy(
                            sendingReset = false,
                            resetNote = "Reset link sent to $masked. Check Inbox and Spam.",
                        ),
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        account = it.account.copy(
                            sendingReset = false,
                            resetError = e.message ?: "Could not send the reset mail.",
                        ),
                    )
                }
            }
        }
    }
}
