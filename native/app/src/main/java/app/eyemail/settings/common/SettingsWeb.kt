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

    // Each page has its own folder, and the folder address serves it.
    const val TERMS = "${BASE}terms/"
    const val PRIVACY = "${BASE}privacy/"
    const val SECURITY = "${BASE}security/"
    const val DATA_PRIVACY = "${BASE}data-privacy/"

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
