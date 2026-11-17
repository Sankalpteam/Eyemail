package app.eyemail.home.storage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
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

/** The free plan meter, and the warning when it is full. Says the same words as the web. */
@Composable
fun StorageMeter(usedBytes: Long, limitBytes: Long, modifier: Modifier = Modifier) {
    val usedMb = usedBytes / (1024 * 1024)
    val limitGb = (limitBytes / (1024 * 1024 * 1024)).coerceAtLeast(1)
    val pct = if (limitBytes > 0) (usedBytes.toFloat() / limitBytes).coerceIn(0f, 1f) else 0f

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "$usedMb MB / $limitGb GB",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ey.Text2,
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .background(Ey.Surface2, RoundedCornerShape(4.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct.coerceAtLeast(0.02f))
                    .height(7.dp)
                    .background(if (pct >= 1f) Ey.Danger else Ey.Accent, RoundedCornerShape(4.dp)),
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "FREE PLAN",
                fontSize = 9.5.sp,
                letterSpacing = 0.9.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ey.Accent,
                modifier = Modifier
                    .background(Ey.AccentSoft, RoundedCornerShape(5.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text("1 GB included", fontSize = 10.5.sp, color = Ey.Text3)
        }
    }
}

/** The red strip that sits above the list when the mailbox cannot take more mail. */
@Composable
fun FullStorageBanner(usedBytes: Long, limitBytes: Long, modifier: Modifier = Modifier) {
    val usedMb = usedBytes / (1024 * 1024)
    val limitGb = (limitBytes / (1024 * 1024 * 1024)).coerceAtLeast(1)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Ey.DangerSoft)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            "Free plan storage full - $usedMb MB of $limitGb GB",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ey.Danger,
        )
        Text(
            "New mail will bounce until you free space. Empty Trash to get it back.",
            fontSize = 11.sp,
            color = Ey.Danger,
        )
    }
}
