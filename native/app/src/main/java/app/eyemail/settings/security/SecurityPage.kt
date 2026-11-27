package app.eyemail.settings.security

import androidx.compose.runtime.Composable
import app.eyemail.settings.common.SettingsWebPage

/**
 * Security, one folder and one file, the same shape as the web folder.
 *
 * The text is not in the app: the page is loaded from eyemail.in, so a correction on the
 * web is a correction on every phone without a new build.
 */
object SecurityPage {
    const val URL = "https://eyemail.in/settings/security/"

    @Composable
    fun Show(onBack: () -> Unit) {
        SettingsWebPage(url = URL, title = "Security", onBack = onBack)
    }
}
