package app.eyemail.authentication

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eyemail.Ey

/**
 * The one box that must be ticked before an account is created or a sign in goes through.
 *
 * The sentence appears once. The two names inside it are blue and tappable, so nothing is
 * repeated under it, and a person can read the words before accepting them. It is never
 * pre-ticked, because a box ticked for you is not agreement.
 */
@Composable
fun TermsBox(
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val termsTag = "terms"
    val privacyTag = "privacy"
    val line = buildAnnotatedString {
        append("I agree to the ")
        pushStringAnnotation(termsTag, "terms")
        withStyle(SpanStyle(color = Ey.Accent, fontWeight = FontWeight.SemiBold)) {
            append("Terms of Use")
        }
        pop()
        append(" and the ")
        pushStringAnnotation(privacyTag, "privacy")
        withStyle(SpanStyle(color = Ey.Accent, fontWeight = FontWeight.SemiBold)) {
            append("Privacy Policy")
        }
        pop()
        append(".")
    }

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
        ClickableText(
            text = line,
            style = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, color = Ey.Text2),
            onClick = { offset ->
                when {
                    line.getStringAnnotations(termsTag, offset, offset).isNotEmpty() -> onOpenTerms()
                    line.getStringAnnotations(privacyTag, offset, offset).isNotEmpty() -> onOpenPrivacy()
                    else -> onChange(!checked)   // tapping the words ticks the box
                }
            },
        )
    }
}
