package in.eyemail.app.settings.appearance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import in.eyemail.app.settings.SectionTitle
import in.eyemail.app.settings.SegmentedRow
import in.eyemail.app.settings.SettingRow

@Composable
fun AppearanceSection(
    state: AppearanceState,
    onTheme: (String) -> Unit,
    onDensity: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(14.dp)) {
        SectionTitle("Appearance")
        SettingRow("Theme", "Light or dark") {
            SegmentedRow(
                options = listOf("light" to "Light", "dark" to "Dark"),
                selected = state.theme,
                onSelect = onTheme,
            )
        }
        SettingRow("Density", "Row height in the list") {
            SegmentedRow(
                options = listOf("comfortable" to "Comfortable", "compact" to "Compact"),
                selected = state.density,
                onSelect = onDensity,
            )
        }
    }
}
