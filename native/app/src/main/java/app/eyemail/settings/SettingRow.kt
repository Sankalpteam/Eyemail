package app.eyemail.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eyemail.Ey

/** One option, drawn as its own card so nobody reads the list as one paragraph. */
@Composable
fun SettingRow(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    control: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .background(Ey.Surface, RoundedCornerShape(12.dp))
            .border(1.dp, Ey.Border, RoundedCornerShape(12.dp))
            .padding(14.dp),
    ) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ey.Text)
        Spacer(Modifier.height(3.dp))
        Text(subtitle, fontSize = 11.6.sp, lineHeight = 16.sp, color = Ey.Text3)
        if (control != null) {
            Spacer(Modifier.height(9.dp))
            control()
        }
    }
}

/** A two or three way choice, drawn the same as the web's segmented control. */
@Composable
fun SegmentedRow(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(Ey.Surface2, RoundedCornerShape(9.dp))
            .border(1.dp, Ey.Border, RoundedCornerShape(9.dp))
            .padding(3.dp),
    ) {
        options.forEach { (key, label) ->
            val on = key == selected
            Text(
                label,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (on) Ey.Surface else Ey.Text2,
                modifier = Modifier
                    .background(if (on) Ey.Text else Ey.Surface2, RoundedCornerShape(7.dp))
                    .clickable { onSelect(key) }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            )
        }
    }
}

@Composable
fun SmallButton(text: String, onClick: () -> Unit, enabled: Boolean = true, modifier: Modifier = Modifier) {
    Text(
        text,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold,
        color = if (enabled) Ey.Accent else Ey.Text3,
        modifier = modifier
            .background(Ey.Surface, RoundedCornerShape(8.dp))
            .border(1.dp, Ey.Border, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 8.dp),
    )
}

@Composable
fun Tag(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        fontSize = 9.5.sp,
        letterSpacing = 0.8.sp,
        fontWeight = FontWeight.SemiBold,
        color = Ey.Accent,
        modifier = modifier
            .background(Ey.AccentSoft, RoundedCornerShape(5.dp))
            .padding(horizontal = 7.dp, vertical = 4.dp),
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        fontSize = 10.sp,
        letterSpacing = 1.2.sp,
        fontWeight = FontWeight.Medium,
        color = Ey.Text3,
        modifier = modifier.padding(bottom = 12.dp),
    )
}

@Composable
fun RowActions(content: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) { content() }
}
