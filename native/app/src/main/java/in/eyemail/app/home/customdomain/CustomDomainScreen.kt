package in.eyemail.app.home.customdomain

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import in.eyemail.app.Ey

/**
 * Custom domain. Honest for now: the records exist, sending as a customer domain
 * does not. Nothing here pretends otherwise.
 */
@Composable
fun CustomDomainScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Custom domain", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ey.Text)
        Spacer(Modifier.height(8.dp))
        Text(
            "Every address today lives on eyemail.in. Sending as your own company domain is not live yet.",
            fontSize = 12.5.sp,
            color = Ey.Text2,
        )
        Spacer(Modifier.height(16.dp))

        listOf("MX" to "route.eyemail.in", "SPF" to "v=spf1 include:eyemail.in ~all", "DKIM" to "added when a domain is verified")
            .forEach { (kind, value) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .background(Ey.Surface2, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                ) {
                    Text(kind, fontSize = 9.5.sp, letterSpacing = 1.sp, color = Ey.Accent, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(value, fontSize = 11.5.sp, color = Ey.Text2)
                }
            }

        Spacer(Modifier.height(8.dp))
        Text(
            "This screen reads the records. It does not claim a domain is working until it is.",
            fontSize = 11.sp,
            color = Ey.Text3,
        )
    }
}
