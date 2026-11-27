package app.eyemail.settings.common

/**
 * The addresses of the settings pages.
 *
 * The app holds no settings text at all. Each option opens the live page on the web, so a
 * correction on the web reaches every phone without a new build, and the two can never say
 * different things.
 */
object SettingsWeb {
    private const val BASE = "https://eyemail.in/settings/"

    const val ACCOUNT = "${BASE}account.html"
    const val APPEARANCE = "${BASE}appearance.html"
    const val MAIL = "${BASE}mail.html"
    const val NOTIFICATIONS = "${BASE}notifications.html"
    const val STORAGE = "${BASE}storage.html"
    const val ABOUT = "${BASE}about.html"

    // The four legal pages carry their own address in their own folder, so nothing is
    // repeated here: terms/TermsPage.kt, privacy/PrivacyPage.kt, security/SecurityPage.kt,
    // dataprivacy/DataPrivacyPage.kt.

    /** The page for a settings section, by its name in the list. */
    fun forSection(section: String): String = when (section) {
        "account" -> ACCOUNT
        "appearance" -> APPEARANCE
        "mail" -> MAIL
        "notifications" -> NOTIFICATIONS
        "storage" -> STORAGE
        "about" -> ABOUT
        else -> BASE
    }
}
