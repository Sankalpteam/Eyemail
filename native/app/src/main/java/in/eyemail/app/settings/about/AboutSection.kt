package in.eyemail.app.settings.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import in.eyemail.app.settings.SectionTitle
import in.eyemail.app.settings.SettingRow
import in.eyemail.app.settings.Tag

@Composable
fun AboutSection(
    state: AboutState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(14.dp)) {
        SectionTitle("About")
        SettingRow("Eyemail", "Business mail on your own domain") { Text(state.version) }
        SettingRow("Domain", "All addresses live here") { Text("@${state.domain}") }
        SettingRow("Built by", "Powered by Indo Workspace") { Tag("INDO") }
        SettingRow("Free plan", "1 GB storage, 50 outside mails a day, 20 recipients a message") { Tag("NO CARD") }
        SettingRow("Not supported yet", "Attachments, and sending as a customer domain") { Tag("LATER") }
        SettingRow("Package", "The app id on the Play Store") { Text(state.packageName) }
    }
}
