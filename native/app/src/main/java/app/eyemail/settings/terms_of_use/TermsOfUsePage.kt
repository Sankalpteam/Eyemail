package app.eyemail.settings.terms_of_use

import androidx.compose.runtime.Composable
import app.eyemail.settings.common.SettingsWebPage

/**
 * Terms of Use. One folder, one file, the same name as the page, on both sides.
 *
 * The text is not in the app: the page is loaded from eyemail.in, so a correction on the
 * web is a correction on every phone without a new build.
 */
object TermsOfUsePage {
    const val TITLE = "Terms of Use"
    const val URL = "https://eyemail.in/settings/terms-of-use/"

    @Composable
    fun Show(onBack: () -> Unit) {
        SettingsWebPage(url = URL, title = TITLE, onBack = onBack)
    }
}
