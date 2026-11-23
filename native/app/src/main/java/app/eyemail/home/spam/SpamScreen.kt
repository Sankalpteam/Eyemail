package app.eyemail.home.spam

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.eyemail.home.Mail
import app.eyemail.home.MailList

/** The spam folder. Its own folder, its own file, its own empty words. */
@Composable
fun SpamScreen(
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
            showRecipient = false,
            emptyText = "No spam. Good.",
            onOpen = onOpen,
        )
    }
}
