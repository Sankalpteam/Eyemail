package app.eyemail.authentication

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eyemail.Ey

/**
 * The one box that must be ticked before an account is created or a sign in goes through.
 *
 * It is never pre-ticked, because a box ticked for you is not agreement. The two names are
 * shown as text here; the pages themselves open from Settings, so the wording lives in one
 * place, on the web, and this screen cannot drift from it.
 */
@Composable
fun TermsBox(
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onChange,
            modifier = Modifier.size(20.dp),
            colors = CheckboxDefaults.colors(checkedColor = Ey.Accent, uncheckedColor = Ey.BorderStrong),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "I agree to the Terms of Use and the Privacy Policy.",
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = Ey.Text2,
        )
    }
}
