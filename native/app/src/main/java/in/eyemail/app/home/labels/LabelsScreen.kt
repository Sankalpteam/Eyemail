package in.eyemail.app.home.labels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import in.eyemail.app.Ey
import in.eyemail.app.authentication.PrimaryButton
import in.eyemail.app.home.Mail
import in.eyemail.app.home.MailList

/**
 * Labels. The panel shows them under LABELS; this is where they are made and used.
 * A label is a name; mail carries the names in its labels field.
 */
@Composable
fun LabelsScreen(
    mails: List<Mail>,
    selectedLabel: String?,
    onCreate: (String) -> Unit,
    onSelect: (String) -> Unit,
    onOpen: (Mail) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember { mutableStateOf("") }
    val names = mails.flatMap { it.labels }.distinct().sorted()

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        Spacer(Modifier.height(12.dp))
        Text("Labels", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ey.Text)
        Spacer(Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                singleLine = true,
                placeholder = { Text("New label name", fontSize = 12.5.sp, color = Ey.Text3) },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Ey.Accent,
                    unfocusedBorderColor = Ey.Border,
                    focusedContainerColor = Ey.Surface,
                    unfocusedContainerColor = Ey.Surface,
                ),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.height(0.dp))
            Column(modifier = Modifier.padding(start = 8.dp)) {
                PrimaryButton(
                    text = "Add",
                    enabled = draft.isNotBlank(),
                    onClick = {
                        onCreate(draft.trim())
                        draft = ""
                    },
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (names.isEmpty()) {
            Text("No labels yet.", fontSize = 12.sp, color = Ey.Text3)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                names.forEach { name ->
                    val on = name == selectedLabel
                    Text(
                        name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (on) Ey.Accent else Ey.Text2,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .then(
                                if (on) Modifier else Modifier,
                            ),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Tap a label in the panel to see its mail.",
                fontSize = 11.sp,
                color = Ey.Text3,
            )
        }

        Spacer(Modifier.height(12.dp))

        val shown = if (selectedLabel == null) mails else mails.filter { it.labels.contains(selectedLabel) }
        MailList(
            mails = shown,
            emptyText = if (selectedLabel == null) "Pick a label, or add one above." else "No mail with that label.",
            onOpen = onOpen,
        )
    }
}
