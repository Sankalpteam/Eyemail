package app.eyemail.settings.terms

import androidx.compose.runtime.Composable
import app.eyemail.settings.common.SettingsWebPage

/**
 * Terms of Use, one folder and one file, the same shape as the web folder.
 *
 * The text is not in the app: the page is loaded from eyemail.in, so a correction on the
 * web is a correction on every phone without a new build.
 */
object TermsPage {
    const val URL = "https://eyemail.in/settings/terms/"

    @Composable
    fun Show(onBack: () -> Unit) {
        SettingsWebPage(url = URL, title = "Terms of Use", onBack = onBack)
    }
}
