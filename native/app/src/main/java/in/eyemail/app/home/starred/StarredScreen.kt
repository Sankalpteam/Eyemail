package in.eyemail.app.home.starred

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import in.eyemail.app.home.Mail
import in.eyemail.app.home.MailList

/** The starred folder. Its own folder, its own file, its own empty words. */
@Composable
fun StarredScreen(
    mails: List<Mail>,
    onOpen: (Mail) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        MailList(
            mails = mails,
            emptyText = "Nothing starred yet.",
            onOpen = onOpen,
        )
    }
}
