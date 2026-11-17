package in.eyemail.app.settings

import in.eyemail.app.settings.account.AccountState
import in.eyemail.app.settings.about.AboutState
import in.eyemail.app.settings.appearance.AppearanceState
import in.eyemail.app.settings.mail.MailState
import in.eyemail.app.settings.notifications.NotificationsState
import in.eyemail.app.settings.storage.StorageState

/** The six sections of the settings screen, each with the values it edits. */
data class SettingsState(
    val section: Section = Section.Account,
    val account: AccountState = AccountState(),
    val appearance: AppearanceState = AppearanceState(),
    val mail: MailState = MailState(),
    val notifications: NotificationsState = NotificationsState(),
    val storage: StorageState = StorageState(),
    val about: AboutState = AboutState(),
    val loading: Boolean = false,
    val error: String = "",
) {
    enum class Section(val label: String) {
        Account("Account"),
        Appearance("Appearance"),
        Mail("Mail"),
        Notifications("Notifications"),
        Storage("Storage"),
        About("About"),
    }
}
