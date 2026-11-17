package app.eyemail.home.inbox

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.eyemail.home.Mail
import app.eyemail.home.MailList

/** The inbox folder. Its own folder, its own file, its own empty words. */
@Composable
fun InboxScreen(
    mails: List<Mail>,
    onOpen: (Mail) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        MailList(
            mails = mails,
            emptyText = "No mail here yet.",
            onOpen = onOpen,
        )
    }
}
