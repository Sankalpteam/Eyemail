package app.eyemail.settings.dataprivacy

import androidx.compose.runtime.Composable
import app.eyemail.settings.common.SettingsWebPage

/**
 * Data and Privacy, one folder and one file, the same shape as the web folder.
 *
 * The text is not in the app: the page is loaded from eyemail.in, so a correction on the
 * web is a correction on every phone without a new build.
 */
object DataPrivacyPage {
    const val URL = "https://eyemail.in/settings/data-privacy/"

    @Composable
    fun Show(onBack: () -> Unit) {
        SettingsWebPage(url = URL, title = "Data and Privacy", onBack = onBack)
    }
}
