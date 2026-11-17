package app.eyemail.home.reader

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eyemail.Ey
import app.eyemail.home.Mail
import app.eyemail.home.parseIso

/** One mail, read the way the web reader shows it: who, when, then the body. */
@Composable
fun ReaderPane(
    mail: Mail,
    onBack: () -> Unit,
    onStar: () -> Unit,
    onTrash: () -> Unit,
    onSpam: () -> Unit,
    onReply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().background(Ey.Surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Ey.Surface2)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ActionChip("Back", onBack)
            ActionChip(if (mail.starred) "Starred" else "Star", onStar)
            ActionChip("Spam", onSpam)
            ActionChip("Trash", onTrash)
            Spacer(Modifier.weight(1f))
            ActionChip("Reply", onReply)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
            .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Text(
                mail.subject,
                fontSize = 21.sp,
                lineHeight = 27.sp,
                letterSpacing = (-0.5).sp,
                fontWeight = FontWeight.Bold,
                color = Ey.Text,
            )
            Spacer(Modifier.height(10.dp))
            Text("FROM", fontSize = 9.5.sp, letterSpacing = 1.sp, color = Ey.Text3)
            Text(mail.fromName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ey.Text)
            Text(mail.from, fontSize = 11.5.sp, color = Ey.Text2)
            Spacer(Modifier.height(8.dp))
            Text("TO", fontSize = 9.5.sp, letterSpacing = 1.sp, color = Ey.Text3)
            Text(mail.to, fontSize = 11.5.sp, color = Ey.Text2)
            Spacer(Modifier.height(8.dp))
            Text("RECEIVED", fontSize = 9.5.sp, letterSpacing = 1.sp, color = Ey.Text3)
            Text(longDate(mail.createdAt), fontSize = 11.5.sp, color = Ey.Text2)
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Ey.Border))
            Spacer(Modifier.height(16.dp))
            Text(mail.body.ifBlank { mail.snippet }, fontSize = 13.5.sp, lineHeight = 22.sp, color = Ey.Text)
            Spacer(Modifier.height(40.dp))
        }
    }
}

/** The web writes a full readable date in the reader, so the app does the same. */
private fun longDate(iso: String): String {
    val parsed = parseIso(iso) ?: return iso
    return java.text.SimpleDateFormat("d MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(parsed)
}

@Composable
private fun ActionChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(Ey.Surface, RoundedCornerShape(8.dp))
            .border(1.dp, Ey.Border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
    ) {
        Text(text, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Ey.Text2)
    }
}
