package app.eyemail.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
            Text("Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ey.Text)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onBack) {
                Text("Close", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Ey.Accent)
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Ey.Surface)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(SettingsState.Section.entries.toList()) { section ->
                val on = section == state.section
                Text(
                    section.label,
                    fontSize = 11.6.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (on) Ey.Accent else Ey.Text2,
                    modifier = Modifier
                        .background(if (on) Ey.AccentSoft else Ey.Surface, RoundedCornerShape(99.dp))
                        .border(1.dp, if (on) Ey.AccentSoft else Ey.Border, RoundedCornerShape(99.dp))
                        .clickable { viewModel.select(section) }
                        .padding(horizontal = 11.dp, vertical = 8.dp),
                )
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
