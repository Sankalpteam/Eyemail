package app.eyemail.home.trash

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eyemail.Ey
import app.eyemail.settings.SettingsStore
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.eyemail.home.Mail
import app.eyemail.home.MailList

/** The trash folder. Its own folder, its own file, its own empty words. */
@Composable
fun TrashScreen(
    mails: List<Mail>,
    onOpen: (Mail) -> Unit,
    onEmptyTrash: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Set<String> = emptySet(),
    onToggle: (String) -> Unit = {},
) {
    // Ask before deleting forever is a setting; when it is off, Empty trash goes at once.
    val mustAsk = SettingsStore.confirmDelete()
    var asking by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        if (mails.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Ey.Surface2)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${mails.size} in Trash", fontSize = 11.5.sp, color = Ey.Text3, modifier = Modifier.weight(1f))
                if (asking) {
                    Text(
                        "Delete forever",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ey.Danger,
                        modifier = Modifier
                            .clickable { asking = false; onEmptyTrash() }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                    )
                    Text(
                        "Cancel",
                        fontSize = 11.5.sp,
                        color = Ey.Text3,
                        modifier = Modifier
                            .clickable { asking = false }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                    )
                } else {
                    Text(
                        "Empty trash",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ey.Danger,
                        modifier = Modifier
                            .clickable { if (mustAsk) asking = true else onEmptyTrash() }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                    )
                }
            }
        }
        MailList(
            mails = mails,
            selected = selected,
            onToggle = onToggle,
            emptyText = "Trash is empty.",
            onOpen = onOpen,
        )
    }
}
