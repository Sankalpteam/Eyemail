package app.eyemail.home.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eyemail.Ey
import app.eyemail.authentication.Alert
import app.eyemail.authentication.PrimaryButton
import app.eyemail.settings.SettingsStore

/** New mail. Send goes through the backend, the same call the web composer makes. */
@Composable
fun ComposePane(
    initialTo: String = "",
    initialSubject: String = "",
    initialBody: String = "",
    onSend: (to: String, subject: String, body: String) -> Unit,
    onCancel: () -> Unit,
    error: String = "",
    busy: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val undoSeconds = SettingsStore.undoSeconds()
    var waiting by remember { mutableStateOf(0) }

    var to by remember { mutableStateOf(initialTo) }
    var subject by remember { mutableStateOf(initialSubject) }
    // The web appends the saved signature to a new mail; the app does the same.
    var body by remember {
        mutableStateOf(
            if (initialBody.isBlank()) {
                val sig = SettingsStore.signature()
                if (sig.isBlank()) "" else "\n\n$sig"
            } else {
                initialBody
            },
        )
    }

    LaunchedEffect(waiting) {
        if (waiting <= 0) return@LaunchedEffect
        kotlinx.coroutines.delay(1000)
        waiting -= 1
        if (waiting == 0) onSend(to.trim(), subject.trim(), body)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Ey.Surface)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("New mail", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Ey.Text)
        Spacer(Modifier.height(14.dp))

        ComposeField("To", to, { to = it }, "name@example.com", KeyboardType.Email, ImeAction.Next)
        Spacer(Modifier.height(10.dp))
        ComposeField("Subject", subject, { subject = it }, "Subject", KeyboardType.Text, ImeAction.Next)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            placeholder = { Text("Write your message...", fontSize = 13.5.sp, color = Ey.Text3) },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(11.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Ey.Accent,
                unfocusedBorderColor = Ey.BorderStrong,
                focusedContainerColor = Ey.Surface,
                unfocusedContainerColor = Ey.Surface,
            ),
            modifier = Modifier.fillMaxWidth().height(220.dp),
        )

        Spacer(Modifier.height(10.dp))
        Alert(error, isError = true)
        Spacer(Modifier.height(12.dp))

        if (waiting > 0) {
            // Undo send, the same wait the web gives: the mail has not left yet.
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(
                    "Sending in ${waiting}s",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ey.Text2,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { waiting = 0 }) {
                    Text("Undo", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Ey.Accent)
                }
            }
        } else {
            PrimaryButton(
                text = if (busy) "Sending..." else "Send",
                loading = busy,
                enabled = to.contains("@") && subject.isNotBlank() && body.isNotBlank() && !busy,
                onClick = {
                    if (undoSeconds <= 0) {
                        onSend(to.trim(), subject.trim(), body)
                    } else {
                        waiting = undoSeconds
                    }
                },
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onCancel, enabled = !busy) {
                Text("Cancel", fontSize = 12.5.sp, color = Ey.Text3)
            }
        }
    }
}

@Composable
private fun ComposeField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    keyboard: KeyboardType,
    ime: ImeAction,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Ey.Text2)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            placeholder = { Text(placeholder, fontSize = 13.sp, color = Ey.Text3) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboard, imeAction = ime),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(11.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Ey.Accent,
                unfocusedBorderColor = Ey.BorderStrong,
                focusedContainerColor = Ey.Surface,
                unfocusedContainerColor = Ey.Surface,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
