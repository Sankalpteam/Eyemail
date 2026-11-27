package app.eyemail.home.labels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import app.eyemail.Ey
import app.eyemail.authentication.PrimaryButton
import app.eyemail.home.Mail
import app.eyemail.home.MailList

/**
 * Labels. The panel shows them under LABELS; this is where they are made and used.
 * A label is a name; mail carries the names in its labels field.
 */
@Composable
fun LabelsScreen(
    mails: List<Mail>,
    labels: List<String>,
    selectedLabel: String?,
    onCreate: (String) -> Unit,
    onSelect: (String?) -> Unit,
    onOpen: (Mail) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember { mutableStateOf("") }
    val names = labels.sorted()

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
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(names) { name ->
                    val on = name == selectedLabel
                    Text(
                        name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (on) Ey.Text2 else Ey.Text2,
                        modifier = Modifier
                            .background(if (on) Ey.AccentSoft else Ey.Surface2, RoundedCornerShape(99.dp))
                            .border(1.dp, if (on) Ey.Accent else Ey.Border, RoundedCornerShape(99.dp))
                            .clickable { onSelect(if (on) null else name) }
                            .padding(horizontal = 11.dp, vertical = 7.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Tap a label to see the mail that carries it.",
                fontSize = 11.sp,
                color = Ey.Text3,
            )
        }

        Spacer(Modifier.height(12.dp))

        val labelled = mails.filter { it.folder != "trash" }
        val shown = if (selectedLabel == null) labelled else labelled.filter { it.labels.contains(selectedLabel) }
        MailList(
            mails = shown,
            emptyText = if (selectedLabel == null) "Pick a label, or add one above." else "No mail with that label.",
            onOpen = onOpen,
        )
    }
}
