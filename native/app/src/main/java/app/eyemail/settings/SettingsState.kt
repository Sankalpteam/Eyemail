package app.eyemail.settings

import app.eyemail.settings.account.AccountState
import app.eyemail.settings.about.AboutState
import app.eyemail.settings.appearance.AppearanceState
import app.eyemail.settings.mail.MailState
import app.eyemail.settings.notifications.NotificationsState
import app.eyemail.settings.storage.StorageState

/** The six sections of the settings screen, each with the values it edits. */
data class SettingsState(
    val section: Section = Section.Account,
    /** Null shows the list. A section here shows that section's own screen. */
    val open: Section? = null,
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
