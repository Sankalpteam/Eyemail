package app.eyemail.settings.storage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eyemail.Ey
import app.eyemail.settings.SectionTitle
import app.eyemail.settings.SettingRow

@Composable
fun StorageSection(
    state: StorageState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(14.dp)) {
        SectionTitle("Storage")

        SettingRow("Used", "${state.usedMb} MB / ${state.limitGb} GB") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Ey.Surface2, RoundedCornerShape(5.dp)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(state.percent.coerceIn(0.02f, 1f))
                        .height(8.dp)
                        .background(if (state.percent >= 1f) Ey.Danger else Ey.Accent, RoundedCornerShape(5.dp)),
                )
            }
        }

        Text(
            "Free plan storage: ${state.limitGb} GB of mail. Deleting a mail frees its space; emptying Trash is " +
                "what actually gives it back, and Trash is cleared automatically after 30 days.",
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = Ey.Text3,
        )
        Spacer(Modifier.height(8.dp))
    }
}
