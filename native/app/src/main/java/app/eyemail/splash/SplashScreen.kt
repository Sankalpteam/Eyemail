package app.eyemail.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eyemail.Ey
import app.eyemail.authentication.EyemailMark

/**
 * The screen the app opens on.
 *
 * Android's own launch screen can only show the icon, and on Android 12 and up it cannot
 * carry a line of text at all. So the name of the company we are built by would never
 * appear. This screen is drawn by the app itself for a moment after the icon splash, which
 * is the only way to put "Powered by Indo Workspace" on it.
 *
 * The layout is deliberate: the mark in the middle, the product name under it, and the
 * company line in the lower middle, not at the very bottom, where a long phone would push
 * it out of sight.
 */
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ey.Bg),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EyemailMark(size = 84)
            Spacer(Modifier.height(16.dp))
            Text(
                "Eyemail",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.4).sp,
                color = Ey.Text,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Business mail on your own domain",
                fontSize = 12.5.sp,
                color = Ey.Text2,
                textAlign = TextAlign.Center,
            )
        }

        // Lower middle, so it is seen on every screen height.
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "POWERED BY INDO WORKSPACE",
                fontSize = 10.5.sp,
                letterSpacing = 1.26.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = Ey.Text3,
                textAlign = TextAlign.Center,
            )
        }
    }
}
