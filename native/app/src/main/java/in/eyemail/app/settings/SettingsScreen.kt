package in.eyemail.app.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import in.eyemail.app.Ey
import in.eyemail.app.settings.account.AccountSection
import in.eyemail.app.settings.about.AboutSection
import in.eyemail.app.settings.appearance.AppearanceSection
import in.eyemail.app.settings.mail.MailSection
import in.eyemail.app.settings.notifications.NotificationsSection
import in.eyemail.app.settings.storage.StorageSection

/**
 * Settings, full screen on a phone, with the six sections as tabs across the top
 * and each section in its own folder behind them.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(Ey.Bg)) {
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
                color = Color(0xFF991B1B),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEE4E2))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            when (state.section) {
                SettingsState.Section.Account -> AccountSection(
                    state = state.account,
                    onName = viewModel::setName,
                    onSendReset = viewModel::sendResetLink,
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
