package app.eyemail.settings.security

import androidx.compose.runtime.Composable
import app.eyemail.settings.common.SettingsWebPage

/**
 * Security. One folder, one file, the same name as the page, on both sides.
 *
 * The text is not in the app: the page is loaded from eyemail.in, so a correction on the
 * web is a correction on every phone without a new build.
 */
object SecurityPage {
    const val TITLE = "Security"
    const val URL = "https://eyemail.in/settings/security/"

    @Composable
    fun Show(onBack: () -> Unit) {
        SettingsWebPage(url = URL, title = TITLE, onBack = onBack)
    }
}
