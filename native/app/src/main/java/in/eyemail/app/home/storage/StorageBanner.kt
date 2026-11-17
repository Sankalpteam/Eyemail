package in.eyemail.app.home.storage

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import in.eyemail.app.Ey

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
                    .background(if (pct >= 1f) Color(0xFFDC2626) else Ey.Accent, RoundedCornerShape(4.dp)),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text("Free plan storage", fontSize = 10.sp, letterSpacing = 0.8.sp, color = Ey.Text3)
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
            .background(Color(0xFFFEE4E2))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            "Free plan storage full - $usedMb MB of $limitGb GB",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF991B1B),
        )
        Text(
            "New mail will bounce until you free space. Empty Trash to get it back.",
            fontSize = 11.sp,
            color = Color(0xFF991B1B),
        )
    }
}
