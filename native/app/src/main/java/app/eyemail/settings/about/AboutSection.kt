package app.eyemail.settings.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.eyemail.Ey
import app.eyemail.settings.SectionTitle
import app.eyemail.settings.SettingRow
import app.eyemail.settings.Tag

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
        SettingRow("Build", "The commit this app was built from") {
            Text(
                app.eyemail.BuildConfig.GIT_SHA,
                fontSize = 11.5.sp,
                color = Ey.Text2,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            )
        }
    }
}
