package app.eyemail.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.eyemail.BuildConfig
import app.eyemail.authentication.AuthSessionStore
import app.eyemail.authentication.EyemailAuthService
import app.eyemail.home.MailRepository
import app.eyemail.settings.about.AboutState
import app.eyemail.settings.notifications.NotificationPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val onSignedOutEverywhere: () -> Unit = {},
) : ViewModel() {

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
                notifications = it.notifications.copy(
                    enabled = SettingsStore.notifications(),
                    permissionGranted = NotificationPermission.isGranted(),
                ),
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
        ThemeState.set(value)
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

    /** Ask the server to notify this phone, and report exactly what it answered. */
    fun testPush() {
        _state.update { it.copy(notifications = it.notifications.copy(testing = true, testResult = "")) }
        viewModelScope.launch {
            val result = runCatching { MailRepository.pushTest(AuthSessionStore.token().orEmpty()) }.getOrElse { error ->
                _state.update {
                    it.copy(
                        notifications = it.notifications.copy(
                            testing = false,
                            testOk = false,
                            testResult = error.message ?: "The server could not be reached.",
                        ),
                    )
                }
                return@launch
            }
            val text = when {
                result.tokens == 0 ->
                    "The server has no phone registered for this account. The app never sent its token."
                result.sent > 0 ->
                    "Sent to ${result.sent} device. If nothing appeared, the phone is blocking it."
                else ->
                    "The server has ${result.tokens} device but sending failed: ${result.error}"
            }
            _state.update {
                it.copy(
                    notifications = it.notifications.copy(
                        testing = false,
                        testOk = result.sent > 0,
                        testResult = text,
                    ),
                )
            }
        }
    }

    fun setNotifications(enabled: Boolean, granted: Boolean, blocked: Boolean) {
        SettingsStore.setNotifications(enabled)
        _state.update {
            it.copy(notifications = it.notifications.copy(enabled = enabled, permissionGranted = granted, blocked = blocked))
        }
    }

    fun signOutEverywhere() {
        _state.update { it.copy(account = it.account.copy(signingOutAll = true, actionNote = "", actionError = "")) }
        viewModelScope.launch {
            try {
                EyemailAuthService.signOutAll(AuthSessionStore.token().orEmpty())
                // The tokens are dead on the server, so the session here goes too.
                AuthSessionStore.clear()
                _state.update {
                    it.copy(account = it.account.copy(signingOutAll = false, actionNote = "Every device has been signed out."))
                }
                onSignedOutEverywhere()
            } catch (e: Exception) {
                _state.update {
                    it.copy(account = it.account.copy(signingOutAll = false, actionError = e.message ?: "Could not sign out the other devices."))
                }
            }
        }
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        _state.update { it.copy(account = it.account.copy(deleting = true, actionNote = "", actionError = "")) }
        viewModelScope.launch {
            try {
                EyemailAuthService.deleteAccount(AuthSessionStore.token().orEmpty())
                AuthSessionStore.clear()
                SettingsStore.clear()
                _state.update { it.copy(account = it.account.copy(deleting = false)) }
                onDeleted()
            } catch (e: Exception) {
                _state.update {
                    it.copy(account = it.account.copy(deleting = false, actionError = e.message ?: "Could not delete the account."))
                }
            }
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
