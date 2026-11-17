package in.eyemail.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import in.eyemail.app.Ey
import in.eyemail.app.home.Mail

/** The rows of a folder: sender, subject, snippet, time, and the unread dot. */
@Composable
fun InboxList(
    mails: List<Mail>,
    emptyText: String,
    onOpen: (Mail) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (mails.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(emptyText, fontSize = 12.5.sp, color = Ey.Text3)
        }
        return
    }
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(mails, key = { it.id }) { mail ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpen(mail) }
                    .background(if (mail.read) Ey.Surface else Ey.AccentSoft.copy(alpha = 0.45f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Ey.Accent, RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        initials(mail.fromName),
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.width(11.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            mail.fromName,
                            fontSize = 12.5.sp,
                            fontWeight = if (mail.read) FontWeight.Medium else FontWeight.Bold,
                            color = Ey.Text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(shortTime(mail.createdAt), fontSize = 10.sp, color = Ey.Text3)
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        mail.subject,
                        fontSize = 12.sp,
                        color = Ey.Text2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (mail.snippet.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            mail.snippet,
                            fontSize = 11.sp,
                            color = Ey.Text3,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (!mail.read) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(7.dp)
                            .background(Ey.Accent, CircleShape),
                    )
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(Ey.Border))
        }
    }
}

/** Filter chips above the list, the same four the web offers. */
@Composable
fun FilterChips(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val chips = listOf("all" to "All", "unread" to "Unread", "starred" to "Starred")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        chips.forEach { (key, label) ->
            val on = key == selected
            Box(
                modifier = Modifier
                    .background(if (on) Ey.Accent else Ey.Surface, RoundedCornerShape(99.dp))
                    .border(1.dp, if (on) Ey.Accent else Ey.Border, RoundedCornerShape(99.dp))
                    .clickable { onSelect(key) }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            ) {
                Text(
                    label,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (on) androidx.compose.ui.graphics.Color.White else Ey.Text2,
                )
            }
        }
    }
}

internal fun initials(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "E"
        parts.size == 1 -> parts[0].take(1).uppercase()
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
    }
}

internal fun shortTime(iso: String): String {
    if (iso.isBlank()) return ""
    val date = runCatching { java.time.Instant.parse(iso) }.getOrNull() ?: return ""
    val local = java.time.LocalDateTime.ofInstant(date, java.time.ZoneId.systemDefault())
    val today = java.time.LocalDate.now()
    return if (local.toLocalDate() == today) {
        String.format("%02d:%02d", local.hour, local.minute)
    } else {
        "${local.dayOfMonth}/${local.monthValue}"
    }
}
