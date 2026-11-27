package app.eyemail.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Drafts
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
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
import app.eyemail.home.reader.MailHtml
import app.eyemail.home.reader.ReaderPane
import app.eyemail.home.reader.longDate
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

    // Entering the mailbox checks whose mail this is before showing anything.
    androidx.compose.runtime.LaunchedEffect(Unit) { viewModel.ensureAccount() }
    var composing by remember { mutableStateOf(false) }
    var composeTo by remember { mutableStateOf("") }
    var composeSubject by remember { mutableStateOf("") }
    var composeCc by remember { mutableStateOf<List<String>>(emptyList()) }
    var composeBody by remember { mutableStateOf("") }
    var composeDraftId by remember { mutableStateOf("") }
    var composeBusy by remember { mutableStateOf(false) }
    var labelling by remember { mutableStateOf(false) }
    var newLabel by remember { mutableStateOf("") }
    var composeError by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current

    // Android 13 asks at runtime; without this the notification never shows.
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
    ) { }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!granted) permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

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

    if (labelling && openMail != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { labelling = false },
            title = { Text("Labels", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ey.Text) },
            text = {
                Column {
                    if (state.labels.isEmpty()) {
                        Text(
                            "No labels yet. Type one below.",
                            fontSize = 12.sp,
                            color = Ey.Text3,
                        )
                    }
                    state.labels.forEach { name ->
                        val on = openMail.labels.contains(name)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleLabel(openMail, name) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                if (on) "✓" else "○",
                                fontSize = 14.sp,
                                color = if (on) Ey.Accent else Ey.Text3,
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                name,
                                fontSize = 13.sp,
                                fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
                                color = if (on) Ey.Accent else Ey.Text,
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newLabel,
                        onValueChange = { newLabel = it },
                        singleLine = true,
                        placeholder = { Text("New label", fontSize = 12.5.sp, color = Ey.Text3) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newLabel.isNotBlank()) {
                        viewModel.toggleLabel(openMail, newLabel.trim())
                        newLabel = ""
                    }
                }) { Text("Add", color = Ey.Accent) }
            },
            dismissButton = {
                TextButton(onClick = { labelling = false; newLabel = "" }) {
                    Text("Done", color = Ey.Text3)
                }
            },
        )
    }

    when {
        composing -> ComposePane(
            initialTo = composeTo,
            initialSubject = composeSubject,
            initialBody = composeBody,
            initialCc = composeCc,
            busy = composeBusy,
            error = composeError,
            onCancel = {
                // Abandoned mail goes to Drafts instead of vanishing, like the web.
                viewModel.saveDraft(composeTo, composeSubject, composeBody, composeCc)
                composing = false
                composeError = ""
                composeTo = ""
                composeSubject = ""
                composeBody = ""
                composeCc = emptyList()
            },
            onSend = { to, subject, body, cc ->
                composeBusy = true
                composeError = ""
                viewModel.send(to, subject, body, cc) { error ->
                    composeBusy = false
                    if (error == null) {
                        if (composeDraftId.isNotBlank()) {
                            viewModel.deleteDraft(composeDraftId)
                            composeDraftId = ""
                        }
                        composing = false
                        composeTo = ""
                        composeSubject = ""
                        composeBody = ""
                        composeCc = emptyList()
                    } else {
                        composeError = error
                    }
                }
            },
        )

        openMail != null && state.folder == "drafts" -> {
            // A draft is not something to read, it is something to finish.
            androidx.compose.runtime.LaunchedEffect(openMail.id) {
                composeTo = openMail.to
                composeCc = openMail.cc.split(",", ";").map { it.trim() }.filter { it.contains("@") }
                composeSubject = openMail.subject
                composeBody = openMail.body
                composeDraftId = openMail.id
                composeError = ""
                viewModel.close()
                composing = true
            }
            Box(Modifier.fillMaxSize().background(Ey.Bg))
        }

        openMail != null -> ReaderPane(
            mail = openMail,
            folderTitle = if (state.folder == "labels" && state.selectedLabel != null) {
                state.selectedLabel ?: titleFor(state.folder)
            } else {
                titleFor(state.folder)
            },
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
                composeCc = emptyList()
                composeSubject = if (openMail.subject.startsWith("Re:", true)) {
                    openMail.subject
                } else {
                    "Re: ${openMail.subject}"
                }
                composeError = ""
                composing = true
            },
            onReplyAll = {
                // Everyone who was on the mail, minus ourselves, so a customer and a
                // colleague both see the answer.
                val me = AuthSessionStore.email().orEmpty().lowercase()
                composeTo = openMail.from
                composeCc = listOf(openMail.cc, openMail.to)
                    .flatMap { it.split(",", ";") }
                    .map { it.trim() }
                    .filter { it.contains("@") && it.lowercase() != me && it.lowercase() != openMail.from.lowercase() }
                    .distinct()
                composeSubject = if (openMail.subject.startsWith("Re:", true)) {
                    openMail.subject
                } else {
                    "Re: ${openMail.subject}"
                }
                composeError = ""
                composing = true
            },
            onLabel = { labelling = true },
            onForward = {
                // Forward keeps the original underneath, so the new person has the context.
                composeTo = ""
                composeCc = emptyList()
                composeSubject = if (openMail.subject.startsWith("Fwd:", true)) {
                    openMail.subject
                } else {
                    "Fwd: ${openMail.subject}"
                }
                composeBody = buildString {
                    append("\n\n---------- Forwarded message ----------\n")
                    append("From: ${openMail.fromName} <${openMail.from}>\n")
                    append("Date: ${longDate(openMail.createdAt)}\n")
                    append("Subject: ${openMail.subject}\n\n")
                    append(MailHtml.toText(openMail.body, Ey.Text).text)
                }
                composeError = ""
                composing = true
            },
        )

        else -> ModalNavigationDrawer(
            drawerState = drawer,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = Ey.Surface,
                    modifier = Modifier.fillMaxWidth(0.6f),
                ) {
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
                    title = if (state.searching && state.query.isNotBlank()) "Search" else titleFor(state.folder),
                    count = state.visible.size,
                    loading = state.loading,
                    onMenu = { scope.launch { drawer.open() } },
                    onRefresh = viewModel::load,
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

                if (state.selecting) {
                    BulkBar(
                        count = state.selected.size,
                        folder = state.folder,
                        allSelected = state.selected.size == state.visible.size,
                        onSelectAll = viewModel::selectAll,
                        onClear = viewModel::clearSelection,
                        onAction = viewModel::bulk,
                    )
                }

                if (state.mailboxMissing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Ey.DangerSoft)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        Text(
                            "This sign in has no mailbox behind it. The account may have been deleted. " +
                                "Sign out and create the mailbox again, or sign in with another address.",
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            color = Ey.Danger,
                        )
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
                        "starred" -> StarredScreen(
                            mails = state.visible, onOpen = viewModel::open,
                            selected = state.selected, onToggle = viewModel::toggleSelect,
                            canLoadMore = state.canLoadMore, onLoadMore = viewModel::loadMore,
                        )
                        "sent" -> SentScreen(
                            mails = state.visible, onOpen = viewModel::open,
                            selected = state.selected, onToggle = viewModel::toggleSelect,
                            canLoadMore = state.canLoadMore, onLoadMore = viewModel::loadMore,
                        )
                        "drafts" -> DraftsScreen(
                            mails = state.visible, onOpen = viewModel::open,
                            selected = state.selected, onToggle = viewModel::toggleSelect,
                            canLoadMore = state.canLoadMore, onLoadMore = viewModel::loadMore,
                        )
                        "spam" -> SpamScreen(
                            mails = state.visible, onOpen = viewModel::open,
                            selected = state.selected, onToggle = viewModel::toggleSelect,
                            canLoadMore = state.canLoadMore, onLoadMore = viewModel::loadMore,
                        )
                        "trash" -> TrashScreen(
                            mails = state.visible,
                            onOpen = viewModel::open,
                            onEmptyTrash = viewModel::emptyTrash,
                            selected = state.selected,
                            onToggle = viewModel::toggleSelect,
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
                        else -> InboxScreen(
                            mails = state.visible, onOpen = viewModel::open,
                            selected = state.selected, onToggle = viewModel::toggleSelect,
                            canLoadMore = state.canLoadMore, onLoadMore = viewModel::loadMore,
                        )
                    }
                }

            }
        }
    }
}

private data class BulkAction(
    val key: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

/** The web's bulk bar: the count, Select all, and the actions for this folder. */
@Composable
private fun BulkBar(
    count: Int,
    folder: String,
    allSelected: Boolean,
    onSelectAll: () -> Unit,
    onClear: () -> Unit,
    onAction: (String) -> Unit,
) {
    val actions = when (folder) {
        "trash" -> listOf(
            BulkAction("restore", "Restore", Icons.Outlined.Restore),
            BulkAction("forever", "Delete forever", Icons.Outlined.DeleteForever),
        )
        "spam" -> listOf(
            BulkAction("notspam", "Not spam", Icons.Outlined.MarkEmailRead),
            BulkAction("read", "Mark read", Icons.Outlined.Drafts),
            BulkAction("trash", "Trash", Icons.Outlined.Delete),
        )
        else -> listOf(
            BulkAction("read", "Mark read", Icons.Outlined.MarkEmailRead),
            BulkAction("unread", "Mark unread", Icons.Outlined.MarkEmailUnread),
            BulkAction("star", "Star", Icons.Outlined.StarBorder),
            BulkAction("spam", "Spam", Icons.Outlined.Report),
            BulkAction("trash", "Trash", Icons.Outlined.Delete),
        )
    }

    Column(modifier = Modifier.fillMaxWidth().background(Ey.AccentSoft)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "$count selected",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Ey.Accent,
                modifier = Modifier.weight(1f),
            )
            Text(
                if (allSelected) "Clear" else "Select all",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ey.Accent,
                modifier = Modifier
                    .clickable { if (allSelected) onClear() else onSelectAll() }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            actions.forEach { action ->
                Box(
                    modifier = Modifier
                        .background(Ey.Surface, RoundedCornerShape(9.dp))
                        .border(1.dp, Ey.Border, RoundedCornerShape(9.dp))
                        .clickable { onAction(action.key) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Icon(
                        action.icon,
                        contentDescription = action.label,
                        tint = if (action.key == "forever") Ey.Danger else Ey.Text2,
                        modifier = Modifier.width(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(
    title: String,
    count: Int,
    loading: Boolean,
    onMenu: () -> Unit,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Ey.Surface)
            .padding(start = 4.dp, end = 8.dp, top = 6.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onMenu) {
            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Ey.Text2)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Ey.Text)
            Text(
                if (loading) "Loading..." else "$count messages",
                fontSize = 11.sp,
                color = Ey.Text3,
            )
        }
        IconButton(onClick = onRefresh) {
            Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = Ey.Text3)
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
        // The web panel starts with the mark and the name.
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 6.dp, end = 6.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(34.dp)
                    .height(34.dp)
                    .background(Ey.ButtonGradient, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Email,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.width(17.dp),
                )
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Eyemail", fontSize = 14.5.sp, fontWeight = FontWeight.ExtraBold, color = Ey.Text)
                Text(
                    "EYEMAIL.IN",
                    fontSize = 10.5.sp,
                    letterSpacing = 0.7.sp,
                    color = Ey.Text3,
                )
            }
        }

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
        Box(Modifier.fillMaxWidth().height(1.dp).background(Ey.Border).padding(horizontal = 4.dp))
        Spacer(Modifier.height(10.dp))

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
            Spacer(Modifier.height(8.dp))
            Text(
                "POWERED BY INDO WORKSPACE",
                fontSize = 9.sp,
                letterSpacing = 1.sp,
                color = Ey.Text3,
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
            Icon(
                Icons.Filled.ExitToApp,
                contentDescription = "Sign out",
                tint = Ey.Text3,
                modifier = Modifier.width(18.dp),
            )
        }
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

/** Every row in the panel has a title here; nothing is allowed to fall back silently. */
internal fun titleFor(folder: String): String = when (folder) {
    "inbox" -> "Inbox"
    "starred" -> "Starred"
    "sent" -> "Sent"
    "drafts" -> "Drafts"
    "spam" -> "Spam"
    "trash" -> "Trash"
    "labels" -> "Labels"
    "domain" -> "Custom domain"
    "settings" -> "Settings"
    else -> folder.replaceFirstChar { it.uppercase() }
}
