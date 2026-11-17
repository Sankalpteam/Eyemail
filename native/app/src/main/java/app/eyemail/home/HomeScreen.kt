package app.eyemail.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.eyemail.Ey
import app.eyemail.authentication.AuthSessionStore
import app.eyemail.home.compose.ComposePane
import app.eyemail.home.customdomain.CustomDomainScreen
import app.eyemail.home.drafts.DraftsScreen
import app.eyemail.home.inbox.InboxScreen
import app.eyemail.home.labels.LabelsScreen
import app.eyemail.home.reader.ReaderPane
import app.eyemail.home.sent.SentScreen
import app.eyemail.home.spam.SpamScreen
import app.eyemail.home.starred.StarredScreen
import app.eyemail.home.storage.FullStorageBanner
import app.eyemail.home.storage.StorageMeter
import app.eyemail.home.trash.TrashScreen
import kotlinx.coroutines.launch

private data class Row(val key: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

/** The same rows, in the same order, as the panel on the web. */
private val MAIL_ROWS = listOf(
    Row("inbox", "Inbox", Icons.Filled.Email),
    Row("starred", "Starred", Icons.Filled.Star),
    Row("sent", "Sent", Icons.Filled.Send),
    Row("drafts", "Drafts", Icons.Filled.Edit),
    Row("spam", "Spam", Icons.Filled.Warning),
    Row("trash", "Trash", Icons.Filled.Delete),
)

private val WORKSPACE_ROWS = listOf(
    Row("domain", "Custom domain", Icons.Filled.Info),
    Row("settings", "Settings", Icons.Filled.Settings),
)

/**
 * The mailbox. The menu is the web's left panel, opened by the three lines in the
 * corner, because a phone has no room for a rail. Every row is the same row.
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

    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val openMail = state.openMail

    androidx.activity.compose.BackHandler(enabled = composing || openMail != null) {
        if (composing) {
            composing = false
            composeError = ""
        } else {
            viewModel.close()
        }
    }

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
                    if (error == null) composing = false else composeError = error
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

        else -> ModalNavigationDrawer(
            drawerState = drawer,
            drawerContent = {
                ModalDrawerSheet(drawerContainerColor = Ey.Surface) {
                    MenuPanel(
                        state = state,
                        onCompose = {
                            scope.launch { drawer.close() }
                            composing = true
                            composeError = ""
                        },
                        onFolder = { key ->
                            scope.launch { drawer.close() }
                            if (key == "settings") onSettings() else viewModel.openFolder(key)
                        },
                        onLabel = { name ->
                            scope.launch { drawer.close() }
                            viewModel.openFolder("labels")
                            viewModel.selectLabel(name)
                        },
                        onSignOut = onSignOut,
                    )
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Ey.Bg)
                    .statusBarsPadding(),
            ) {
                Header(
                    title = titleFor(state.folder),
                    address = AuthSessionStore.email().orEmpty(),
                    onMenu = { scope.launch { drawer.open() } },
                )

                if (state.storageFull) {
                    FullStorageBanner(state.storageUsed, state.storageLimit)
                }

                // The web sits the search on the surface with a thin border, not on the page colour.
                Column(modifier = Modifier.fillMaxWidth().background(Ey.Surface)) {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::search,
                        singleLine = true,
                        placeholder = {
                            Text(
                                "Search or try from:razorpay is:unread",
                                fontSize = 12.5.sp,
                                color = Ey.Text3,
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Ey.Accent,
                            unfocusedBorderColor = Ey.Border,
                            focusedContainerColor = Ey.Surface2,
                            unfocusedContainerColor = Ey.Surface2,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp),
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
                            .background(Ey.DangerSoft)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text(state.error, fontSize = 11.5.sp, color = Ey.Danger)
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Ey.Surface)) {
                    when (state.folder) {
                        "starred" -> StarredScreen(mails = state.visible, onOpen = viewModel::open)
                        "sent" -> SentScreen(mails = state.visible, onOpen = viewModel::open)
                        "drafts" -> DraftsScreen(mails = state.visible, onOpen = viewModel::open)
                        "spam" -> SpamScreen(mails = state.visible, onOpen = viewModel::open)
                        "trash" -> TrashScreen(
                            mails = state.visible,
                            onOpen = viewModel::open,
                            onEmptyTrash = viewModel::emptyTrash,
                        )
                        "labels" -> LabelsScreen(
                            mails = state.mails,
                            labels = state.labels,
                            selectedLabel = state.selectedLabel,
                            onCreate = viewModel::createLabel,
                            onSelect = viewModel::selectLabel,
                            onOpen = viewModel::open,
                        )
                        "domain" -> CustomDomainScreen()
                        else -> InboxScreen(mails = state.visible, onOpen = viewModel::open)
                    }
                }

            }
        }
    }
}

@Composable
private fun Header(title: String, address: String, onMenu: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Ey.Surface)
            .padding(start = 4.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onMenu) {
            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Ey.Text2)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Ey.Text)
            Text(address, fontSize = 10.5.sp, color = Ey.Text3)
        }
    }
}

/** The web's left panel, row for row. */
@Composable
private fun MenuPanel(
    state: HomeState,
    onCompose: () -> Unit,
    onFolder: (String) -> Unit,
    onLabel: (String) -> Unit,
    onSignOut: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .background(Ey.ButtonGradient, RoundedCornerShape(12.dp))
                .clickable(onClick = onCompose),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Create,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.width(17.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text("Compose", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(10.dp))

        MAIL_ROWS.forEach { row ->
            MenuRow(
                label = row.label,
                icon = row.icon,
                count = if (row.key == "inbox") state.unreadCount else 0,
                selected = state.folder == row.key,
                onClick = { onFolder(row.key) },
            )
        }

        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, top = 14.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "LABELS",
                fontSize = 10.sp,
                letterSpacing = 1.4.sp,
                color = Ey.Text3,
                modifier = Modifier.weight(1f),
            )
            Text(
                "+",
                fontSize = 15.sp,
                color = Ey.Text3,
                modifier = Modifier.clickable(onClick = { onFolder("labels") }),
            )
        }
        if (state.labels.isEmpty()) {
            Text(
                "No labels yet",
                fontSize = 12.sp,
                color = Ey.Text3,
                modifier = Modifier.padding(start = 8.dp, bottom = 6.dp),
            )
        } else {
            state.labels.forEach { name ->
                MenuRow(
                    label = name,
                    icon = Icons.Filled.Edit,
                    count = 0,
                    selected = state.folder == "labels" && state.selectedLabel == name,
                    onClick = { onLabel(name) },
                )
            }
        }

        Text(
            "WORKSPACE",
            fontSize = 10.sp,
            letterSpacing = 1.4.sp,
            color = Ey.Text3,
            modifier = Modifier.padding(start = 10.dp, top = 14.dp, bottom = 8.dp),
        )
        WORKSPACE_ROWS.forEach { row ->
            MenuRow(
                label = row.label,
                icon = row.icon,
                count = 0,
                selected = state.folder == row.key,
                onClick = { onFolder(row.key) },
            )
        }

        Spacer(Modifier.height(16.dp))

        // The web keeps the meter in the panel, under the workspace rows.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Ey.Surface2, RoundedCornerShape(12.dp))
                .border(1.dp, Ey.Border, RoundedCornerShape(12.dp))
                .padding(12.dp),
        ) {
            StorageMeter(
                usedBytes = state.storageUsed,
                limitBytes = state.storageLimit,
            )
        }

        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
                .clickable(onClick = onSignOut)
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(32.dp)
                    .background(Ey.Accent, RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    initials(AuthSessionStore.email().orEmpty()),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    AuthSessionStore.email().orEmpty().substringBefore("@").ifBlank { "You" },
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ey.Text,
                )
                Text(
                    AuthSessionStore.email().orEmpty(),
                    fontSize = 10.5.sp,
                    color = Ey.Text3,
                )
            }
            Text(
                "Sign out",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Ey.Accent,
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "POWERED BY INDO WORKSPACE",
            fontSize = 9.sp,
            letterSpacing = 1.sp,
            color = Ey.Text3,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun MenuRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .background(if (selected) Ey.AccentSoft else Color.Transparent, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) Ey.Accent else Ey.Text3,
            modifier = Modifier.width(18.dp),
        )
        Spacer(Modifier.width(11.dp))
        Text(
            label,
            fontSize = 13.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Ey.Accent else Ey.Text2,
            modifier = Modifier.weight(1f),
        )
        if (count > 0) {
            Text(
                count.toString(),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = Ey.Accent,
                modifier = Modifier
                    .background(Ey.AccentSoft, RoundedCornerShape(99.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}

private fun titleFor(folder: String): String = when (folder) {
    "inbox" -> "Inbox"
    "starred" -> "Starred"
    "sent" -> "Sent"
    "drafts" -> "Drafts"
    "spam" -> "Spam"
    "trash" -> "Trash"
    "labels" -> "Labels"
    "domain" -> "Custom domain"
    else -> "Inbox"
}
