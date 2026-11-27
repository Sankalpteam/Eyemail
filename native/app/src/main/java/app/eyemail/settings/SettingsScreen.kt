package app.eyemail.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.eyemail.Ey
import app.eyemail.settings.account.AccountSection
import app.eyemail.settings.about.AboutSection
import app.eyemail.settings.appearance.AppearanceSection
import app.eyemail.settings.mail.MailSection
import app.eyemail.settings.notifications.NotificationsSection
import app.eyemail.settings.storage.StorageSection

/**
 * Settings, full screen on a phone, with the six sections as tabs across the top
 * and each section in its own folder behind them.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    onSignedOutEverywhere: () -> Unit,
    viewModel: SettingsViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SettingsViewModel(onSignedOutEverywhere) }
        },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(Ey.Bg).statusBarsPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Ey.Surface)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val open = state.open
            if (open != null) {
                // An arrow, not the word back, and it returns to the list where it was.
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clickable { viewModel.closeSection() },
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to settings",
                        tint = Ey.Text,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(open.label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ey.Text)
            } else {
                Text("Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ey.Text)
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onBack) {
                Text("Close", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Ey.Accent)
            }
        }


        if (state.error.isNotBlank()) {
            Text(
                state.error,
                fontSize = 11.5.sp,
                color = Ey.Danger,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Ey.DangerSoft)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }

        // Each section keeps its own scroll position, and opening a section starts at the top.
        val scroll = rememberScrollState()
        LaunchedEffect(state.section) { scroll.scrollTo(0) }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scroll),
        ) {
            if (state.open == null) {
                // The list. Every section is a row, found by scrolling, and a tap opens it.
                Spacer(Modifier.height(8.dp))
                SettingsState.Section.entries.forEach { section ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            section.label.uppercase(),
                            fontSize = 10.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Ey.Text3,
                            modifier = Modifier.padding(start = 18.dp, top = 14.dp, bottom = 6.dp),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Ey.Surface)
                                .clickable { viewModel.open(section) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                sectionRowTitle(section),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Ey.Text,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                sectionRowSummary(section, state),
                                fontSize = 11.5.sp,
                                color = Ey.Text3,
                            )
                            Spacer(Modifier.width(8.dp))
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Ey.Text3,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "POWERED BY INDO WORKSPACE",
                    fontSize = 9.5.sp,
                    letterSpacing = 1.1.sp,
                    color = Ey.Text3,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
                return@Column
            }

            when (state.section) {
                SettingsState.Section.Account -> AccountSection(
                    state = state.account,
                    onName = viewModel::setName,
                    onSendReset = viewModel::sendResetLink,
                    onSignOutAll = viewModel::signOutEverywhere,
                    onDelete = { viewModel.deleteAccount(onAccountDeleted) },
                )

                SettingsState.Section.Appearance -> AppearanceSection(
                    state = state.appearance,
                    onTheme = viewModel::setTheme,
                    onDensity = viewModel::setDensity,
                )

                SettingsState.Section.Mail -> MailSection(
                    state = state.mail,
                    onSignature = viewModel::setSignature,
                    onUndo = viewModel::setUndo,
                    onConfirmDelete = viewModel::setConfirmDelete,
                )

                SettingsState.Section.Notifications -> NotificationsSection(
                    state = state.notifications,
                    onChange = viewModel::setNotifications,
                    onTest = viewModel::testPush,
                    onCheckLast = viewModel::checkLastPush,
                    onRefresh = viewModel::refreshNotifications,
                )

                SettingsState.Section.Storage -> StorageSection(state = state.storage)

                SettingsState.Section.About -> AboutSection(state = state.about)
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "POWERED BY INDO WORKSPACE",
                fontSize = 9.5.sp,
                letterSpacing = 1.1.sp,
                color = Ey.Text3,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}

/** The row title: what the section is called, in the words a person uses. */
private fun sectionRowTitle(section: SettingsState.Section): String = when (section) {
    SettingsState.Section.Account -> "Name, address and password"
    SettingsState.Section.Appearance -> "Theme and row height"
    SettingsState.Section.Mail -> "Signature, undo and delete"
    SettingsState.Section.Notifications -> "Mail alerts on this phone"
    SettingsState.Section.Storage -> "Space used and free"
    SettingsState.Section.About -> "Version, build and the company"
}

/** The row summary: the one fact worth seeing without opening it. */
private fun sectionRowSummary(section: SettingsState.Section, state: SettingsState): String = when (section) {
    SettingsState.Section.Account -> state.account.email.ifBlank { "" }
    SettingsState.Section.Appearance -> state.appearance.theme.lowercase()
    SettingsState.Section.Mail -> if (state.mail.signature.isBlank()) "no signature" else "signature set"
    SettingsState.Section.Notifications -> if (state.notifications.enabled) "on" else "off"
    SettingsState.Section.Storage -> {
        val used = (state.storage.usedBytes / (1024.0 * 1024.0))
        String.format("%.1f MB used", used)
    }
    SettingsState.Section.About -> state.about.version
}
