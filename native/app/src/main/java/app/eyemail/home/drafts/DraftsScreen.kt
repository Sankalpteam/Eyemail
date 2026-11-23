package app.eyemail.home.drafts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.eyemail.home.Mail
import app.eyemail.home.MailList

/** The drafts folder. Its own folder, its own file, its own empty words. */
@Composable
fun DraftsScreen(
    mails: List<Mail>,
    onOpen: (Mail) -> Unit,
    modifier: Modifier = Modifier,
    selected: Set<String> = emptySet(),
    onToggle: (String) -> Unit = {},
    canLoadMore: Boolean = false,
    onLoadMore: () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxSize()) {
        MailList(
            mails = mails,
            selected = selected,
            onToggle = onToggle,
            canLoadMore = canLoadMore,
            onLoadMore = onLoadMore,
            emptyText = "No drafts yet.",
            onOpen = onOpen,
        )
    }
}
