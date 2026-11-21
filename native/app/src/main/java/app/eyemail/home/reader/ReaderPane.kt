package app.eyemail.home.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
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
import app.eyemail.home.initials
import app.eyemail.home.parseIso

/** One mail, read the way the web reader shows it: who, when, then the body. */
@Composable
fun ReaderPane(
    mail: Mail,
    folderTitle: String,
    onBack: () -> Unit,
    onStar: () -> Unit,
    onTrash: () -> Unit,
    onSpam: () -> Unit,
    onReply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Ey.Surface)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Ey.Surface2)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconChip(Icons.AutoMirrored.Filled.ArrowBack, "Back", onBack)
            Text(
                folderTitle,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ey.Text3,
                modifier = Modifier.padding(start = 2.dp),
            )
            ActionChip(if (mail.starred) "Starred" else "Star", onStar)
            ActionChip("Spam", onSpam)
            ActionChip("Trash", onTrash)
            ActionChip("Reply", onReply)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 14.dp),
        ) {
            Text(
                mail.subject.ifBlank { "(no subject)" },
                fontSize = 21.sp,
                lineHeight = 27.sp,
                letterSpacing = (-0.5).sp,
                fontWeight = FontWeight.Bold,
                color = Ey.Text,
            )
            Spacer(Modifier.height(12.dp))

            // Who it is from, the way our own app draws a row: square avatar, name, time.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Ey.Surface2, RoundedCornerShape(12.dp))
                    .border(1.dp, Ey.Border, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(Ey.Accent, RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        initials(mail.fromName.ifBlank { mail.from }),
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        mail.fromName.ifBlank { mail.from },
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ey.Text,
                    )
                    Text(mail.from, fontSize = 11.5.sp, color = Ey.Text2)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "TO  ${mail.to}",
                        fontSize = 10.5.sp,
                        color = Ey.Text3,
                        maxLines = 1,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(longDate(mail.createdAt), fontSize = 10.5.sp, color = Ey.Text3)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Star",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (mail.starred) Ey.Accent else Ey.Text3,
                        modifier = Modifier
                            .clickable(onClick = onStar)
                            .padding(4.dp),
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // The mail itself, on white, in our type. Light, never dark.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Ey.Surface, RoundedCornerShape(12.dp))
                    .border(1.dp, Ey.Border, RoundedCornerShape(12.dp))
                    .padding(16.dp),
            ) {
                val body = MailHtml.toText(mail.body, Ey.Accent)
                if (body.text.isBlank()) {
                    Text(mail.snippet, fontSize = 13.5.sp, lineHeight = 22.sp, color = Ey.Text2)
                } else {
                    Text(
                        body,
                        fontSize = 13.5.sp,
                        lineHeight = 22.sp,
                        color = Ey.Text,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Ey.AccentSoft, RoundedCornerShape(12.dp))
                    .clickable(onClick = onReply)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Reply to ${mail.fromName.ifBlank { mail.from }}",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ey.Accent,
                )
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

/** The web writes a full readable date in the reader, so the app does the same. */
private fun longDate(iso: String): String {
    val parsed = parseIso(iso) ?: return iso
    return java.text.SimpleDateFormat("d MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(parsed)
}

/** The arrow form of a chip, for Back, the way the web draws it. */
@Composable
private fun IconChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .background(Ey.Surface, RoundedCornerShape(8.dp))
            .border(1.dp, Ey.Border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 6.dp),
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = Ey.Text2,
            modifier = Modifier.width(18.dp),
        )
    }
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
