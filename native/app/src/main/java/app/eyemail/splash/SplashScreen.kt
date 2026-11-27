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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color

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
            // The same size the system splash draws its icon, so the hand over from the
            // system screen to this one looks like one screen, not two.
            // Flat blue, the same colour as the launcher icon the system splash shows,
            // so the two screens read as one.
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFF1857B8), RoundedCornerShape(33.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(64.dp)) {
                    val w = size.width
                    val h = size.height
                    drawRoundRect(
                        color = Color.White,
                        topLeft = androidx.compose.ui.geometry.Offset(w * 0.06f, h * 0.22f),
                        size = androidx.compose.ui.geometry.Size(w * 0.88f, h * 0.56f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.06f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.075f),
                    )
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(w * 0.10f, h * 0.26f)
                        lineTo(w * 0.50f, h * 0.55f)
                        lineTo(w * 0.90f, h * 0.26f)
                    }
                    drawPath(path, Color.White, style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = w * 0.075f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round,
                    ))
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(
                "Eyemail",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.4).sp,
                color = Ey.Text,
                textAlign = TextAlign.Center,
            )
        }

        // Lower middle, so it is seen on every screen height.
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 96.dp),
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
