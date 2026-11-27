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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eyemail.Ey

/**
 * The one box that must be ticked before an account is created or a sign in goes through.
 *
 * It is never pre-ticked, because a box ticked for you is not agreement. The two names are
 * links, so a person can read the words before accepting them, and the pages themselves come
 * from the web, so this screen cannot drift from them.
 */
@Composable
fun TermsBox(
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 2.dp),
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
            buildAnnotatedString {
                append("I agree to the ")
                withStyle(SpanStyle(color = Ey.Accent, fontWeight = FontWeight.SemiBold)) {
                    append("Terms of Use")
                }
                append(" and the ")
                withStyle(SpanStyle(color = Ey.Accent, fontWeight = FontWeight.SemiBold)) {
                    append("Privacy Policy")
                }
                append(".")
            },
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = Ey.Text2,
        )
    }
    Row(modifier = modifier.fillMaxWidth().padding(start = 28.dp, top = 2.dp)) {
        Text(
            "Read the Terms",
            fontSize = 11.5.sp,
            color = Ey.Accent,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { onOpenTerms() },
        )
        Spacer(Modifier.width(12.dp))
        Text(
            "Read the Privacy Policy",
            fontSize = 11.5.sp,
            color = Ey.Accent,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { onOpenPrivacy() },
        )
    }
}
