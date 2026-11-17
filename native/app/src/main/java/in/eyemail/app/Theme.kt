package in.eyemail.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * The same tokens as the web app, so a screen built here matches the browser
 * side by side. Change them here only when the web changes them.
 */
object Ey {
    val Bg = Color(0xFFEEF1F4)
    val Surface = Color(0xFFFFFFFF)
    val Surface2 = Color(0xFFF7F9FB)
    val Border = Color(0xFFE3E8EF)
    val BorderStrong = Color(0xFFCBD5E1)
    val Text = Color(0xFF0F1A2B)
    val Text2 = Color(0xFF44536B)
    val Text3 = Color(0xFF7B8798)
    val Accent = Color(0xFF1857B8)
    val Accent2 = Color(0xFF0F3D86)
    val AccentSoft = Color(0xFFE8EFFB)
    val Danger = Color(0xFFB42318)
    val Ok = Color(0xFF067647)
}

private val LightColors = lightColorScheme(
    primary = Ey.Accent,
    onPrimary = Color.White,
    primaryContainer = Ey.AccentSoft,
    onPrimaryContainer = Ey.Accent2,
    background = Ey.Bg,
    onBackground = Ey.Text,
    surface = Ey.Surface,
    onSurface = Ey.Text,
    surfaceVariant = Ey.Surface2,
    onSurfaceVariant = Ey.Text2,
    outline = Ey.Border,
    outlineVariant = Ey.BorderStrong,
    error = Ey.Danger,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7AA7F0),
    onPrimary = Color(0xFF0B1220),
    background = Color(0xFF0B1220),
    onBackground = Color(0xFFE8EDF5),
    surface = Color(0xFF121A27),
    onSurface = Color(0xFFE8EDF5),
    surfaceVariant = Color(0xFF182231),
    onSurfaceVariant = Color(0xFFAFBCCD),
    outline = Color(0xFF26313F),
    error = Color(0xFFFCA5A5),
)

private val EyTypography = Typography(
    displaySmall = TextStyle(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.6).sp),
    headlineSmall = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp),
    titleMedium = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 17.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.6.sp),
)

@Composable
fun EyemailTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = EyTypography,
        content = content,
    )
}
