package in.eyemail.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import in.eyemail.app.Ey
import in.eyemail.app.authentication.AuthSessionStore
import in.eyemail.app.home.compose.ComposePane
import in.eyemail.app.home.inbox.InboxList
import in.eyemail.app.home.reader.ReaderPane
import in.eyemail.app.home.storage.FullStorageBanner
import in.eyemail.app.home.storage.StorageMeter

private val FOLDERS = listOf(
    "inbox" to "Inbox",
    "starred" to "Starred",
    "sent" to "Sent",
    "drafts" to "Drafts",
    "spam" to "Spam",
    "trash" to "Trash",
)

/**
 * The mailbox: folders across the top, the list under them, and the reader or the
 * composer taking over the screen when one is open. Same shape as the web on a phone.
 */
@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    onSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var composing by remember { mutableStateOf(false) }
    var composeTo by remember { mutableStateOf("") }
    var composeSubject by remember { mutableStateOf("") }
    var composeBusy by remember { mutableStateOf(false) }
    var composeError by remember { mutableStateOf("") }

    val openMail = state.openMail

    when {
        composing -> ComposePane(
            initialTo = composeTo,
            initialSubject = composeSubject,
            busy = composeBusy,
            error = composeError,
            onCancel = {
                composing = false
                composeError = ""
            },
            onSend = { to, subject, body ->
                composeBusy = true
                composeError = ""
                viewModel.send(to, subject, body) { error ->
                    composeBusy = false
                    if (error == null) {
                        composing = false
                    } else {
                        composeError = error
                    }
                }
            },
        )

        openMail != null -> ReaderPane(
            mail = openMail,
            onBack = viewModel::close,
            onStar = { viewModel.toggleStar(openMail) },
            onTrash = {
                viewModel.move(openMail, "trash")
                viewModel.close()
            },
            onSpam = {
                viewModel.move(openMail, "spam")
                viewModel.close()
            },
            onReply = {
                composeTo = openMail.from
                composeSubject = if (openMail.subject.startsWith("Re:", true)) {
                    openMail.subject
                } else {
                    "Re: ${openMail.subject}"
                }
                composeError = ""
                composing = true
            },
        )

        else -> Column(modifier = Modifier.fillMaxSize().background(Ey.Bg)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Ey.Surface)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Eyemail", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Ey.Text)
                    Text(
                        AuthSessionStore.email().orEmpty(),
                        fontSize = 10.5.sp,
                        color = Ey.Text3,
                    )
                }
                TextButton(onClick = { composing = true; composeError = "" }) {
                    Text("Compose", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Ey.Accent)
                }
                TextButton(onClick = onSettings) {
                    Text("Settings", fontSize = 12.5.sp, color = Ey.Text2)
                }
                TextButton(onClick = onSignOut) {
                    Text("Sign out", fontSize = 12.5.sp, color = Ey.Text3)
                }
            }

            if (state.storageFull) {
                FullStorageBanner(state.storageUsed, state.storageLimit)
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Ey.Surface)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(FOLDERS) { (key, label) ->
                    val on = key == state.folder
                    Box(
                        modifier = Modifier
                            .background(if (on) Ey.Accent else Ey.Surface2, RoundedCornerShape(99.dp))
                            .border(1.dp, if (on) Ey.Accent else Ey.Border, RoundedCornerShape(99.dp))
                            .clickable { viewModel.openFolder(key) }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                    ) {
                        Text(
                            if (key == "inbox" && state.unreadCount > 0) "$label ${state.unreadCount}" else label,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (on) Color.White else Ey.Text2,
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::search,
                singleLine = true,
                placeholder = { Text("Search, or from:name is:unread", fontSize = 12.5.sp, color = Ey.Text3) },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Ey.Accent,
                    unfocusedBorderColor = Ey.Border,
                    focusedContainerColor = Ey.Surface,
                    unfocusedContainerColor = Ey.Surface,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${state.visible.size} messages",
                    fontSize = 11.sp,
                    color = Ey.Text3,
                    modifier = Modifier.weight(1f),
                )
                if (state.loading) {
                    Text("Loading...", fontSize = 11.sp, color = Ey.Text3)
                } else {
                    TextButton(onClick = viewModel::load) {
                        Text("Refresh", fontSize = 11.5.sp, color = Ey.Accent)
                    }
                }
            }

            if (state.error.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEE4E2))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(state.error, fontSize = 11.5.sp, color = Color(0xFF991B1B))
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Ey.Surface)) {
                InboxList(
                    mails = state.visible,
                    emptyText = when (state.folder) {
                        "trash" -> "Trash is empty."
                        "spam" -> "No spam. Good."
                        "drafts" -> "No drafts yet."
                        "sent" -> "Nothing sent yet."
                        else -> "No mail here yet."
                    },
                    onOpen = viewModel::open,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Ey.Surface2)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StorageMeter(
                    usedBytes = state.storageUsed,
                    limitBytes = state.storageLimit,
                    modifier = Modifier.width(190.dp),
                )
                Spacer(Modifier.weight(1f))
                Text("Powered by Indo Workspace", fontSize = 8.5.sp, letterSpacing = 0.8.sp, color = Ey.Text3)
            }
        }
    }
}
