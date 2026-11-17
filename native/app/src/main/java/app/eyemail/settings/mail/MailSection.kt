package app.eyemail.settings.mail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eyemail.Ey
import app.eyemail.settings.SectionTitle
import app.eyemail.settings.SegmentedRow
import app.eyemail.settings.SettingRow
import app.eyemail.settings.Tag

@Composable
fun MailSection(
    state: MailState,
    onSignature: (String) -> Unit,
    onUndo: (Int) -> Unit,
    onConfirmDelete: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(14.dp)) {
        SectionTitle("Mail")

        SettingRow("Signature", "Added to the end of every new mail", wide = true) {
            OutlinedTextField(
                value = state.signature,
                onValueChange = onSignature,
                singleLine = true,
                placeholder = { Text("— sent from Eyemail", fontSize = 13.sp, color = Ey.Text3) },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Ey.Accent,
                    unfocusedBorderColor = Ey.Border,
                    focusedContainerColor = Ey.Surface,
                    unfocusedContainerColor = Ey.Surface,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        SettingRow("Undo send", "A short wait before the mail leaves") {
            SegmentedRow(
                options = listOf("0" to "Off", "5" to "5s", "10" to "10s"),
                selected = state.undoSeconds.toString(),
                onSelect = { onUndo(it.toIntOrNull() ?: 0) },
            )
        }

        SettingRow("Ask before deleting forever", "One confirmation before a mail leaves Trash") {
            SegmentedRow(
                options = listOf("yes" to "Ask", "no" to "Do not ask"),
                selected = if (state.confirmDelete) "yes" else "no",
                onSelect = { onConfirmDelete(it == "yes") },
            )
        }

        SettingRow(
            "Free plan sending limit",
            "50 mails a day to addresses outside eyemail.in. It is there to keep spam out, not because mail is short.",
        ) {
            Tag("50 / DAY")
        }
    }
}
