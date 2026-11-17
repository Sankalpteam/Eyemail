package in.eyemail.app.home.inbox

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import in.eyemail.app.home.Mail
import in.eyemail.app.home.MailList

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
