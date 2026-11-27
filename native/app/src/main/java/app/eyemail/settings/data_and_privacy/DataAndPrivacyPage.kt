package app.eyemail.settings.data_and_privacy

import androidx.compose.runtime.Composable
import app.eyemail.settings.common.SettingsWebPage

/**
 * Data and Privacy. One folder, one file, the same name as the page, on both sides.
 *
 * The text is not in the app: the page is loaded from eyemail.in, so a correction on the
 * web is a correction on every phone without a new build.
 */
object DataAndPrivacyPage {
    const val TITLE = "Data and Privacy"
    const val URL = "https://eyemail.in/settings/data-and-privacy/"

    @Composable
    fun Show(onBack: () -> Unit) {
        SettingsWebPage(url = URL, title = TITLE, onBack = onBack)
    }
}
