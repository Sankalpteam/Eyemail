package app.eyemail.home.sent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.eyemail.home.Mail
import app.eyemail.home.MailList

/** The sent folder. Its own folder, its own file, its own empty words. */
@Composable
fun SentScreen(
    mails: List<Mail>,
    onOpen: (Mail) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        MailList(
            mails = mails,
            emptyText = "Nothing sent yet.",
            onOpen = onOpen,
        )
    }
}
