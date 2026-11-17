package app.eyemail

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.eyemail.R
import app.eyemail.settings.SettingsStore

/**
 * The same tokens as the web app, so a screen built here matches the browser
 * side by side. Change them here only when the web changes them.
 */
object Ey {
    val Bg = Color(0xFFEEF1F4)
    val Surface = Color(0xFFFFFFFF)
    val Surface2 = Color(0xFFF7F9FA)
    val Border = Color(0xFFEAEAEC)
    val BorderStrong = Color(0xFFD9DADD)
    val Text = Color(0xFF101A2B)
    val Text2 = Color(0xFF4A5A6A)
    val Text3 = Color(0xFF8494A3)
    val Accent = Color(0xFF1857B8)
    val Accent2 = Color(0xFF0F3D86)
    val AccentSoft = Color(0xFFE8EFFB)
    val Danger = Color(0xFFB42318)
    /** The button gradient, straight from the web: 150deg, accent to accent-2. */
    val ButtonGradient = Brush.linearGradient(listOf(Color(0xFF1857B8), Color(0xFF0F3D86)))
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

/**
 * Plus Jakarta Sans, the same family the web page loads, bundled so the app has it
 * offline and on the first frame. The weights match the web: 400, 500, 600, 700, 800.
 */
private val Jakarta = FontFamily(
    Font(R.font.jakarta_regular, FontWeight.Normal),
    Font(R.font.jakarta_medium, FontWeight.Medium),
    Font(R.font.jakarta_semibold, FontWeight.SemiBold),
    Font(R.font.jakarta_bold, FontWeight.Bold),
    Font(R.font.jakarta_extrabold, FontWeight.ExtraBold),
)

private val EyTypography = Typography(
    // h1 26px, -.03em, in the web's heading font
    displaySmall = TextStyle(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.78).sp),
    headlineSmall = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp),
    titleMedium = TextStyle(fontSize = 14.sp, lineHeight = 19.sp, fontWeight = FontWeight.SemiBold),
    // .sub 13px
    bodyMedium = TextStyle(fontSize = 13.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontSize = 12.5.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal),
    // .btn 13.5px 700
    labelLarge = TextStyle(fontSize = 13.5.sp, lineHeight = 17.sp, fontWeight = FontWeight.Bold),
    // the POWERED BY line: 10.5px, .12em, monospace
    labelSmall = TextStyle(fontSize = 10.5.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.26.sp),
)

@Composable
fun EyemailTheme(
    dark: Boolean = SettingsStore.theme() == "dark",
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = EyTypography.withFamily(Jakarta),
        content = content,
    )
}

private fun Typography.withFamily(family: FontFamily): Typography = Typography(
    displayLarge = displayLarge.copy(fontFamily = family),
    displayMedium = displayMedium.copy(fontFamily = family),
    displaySmall = displaySmall.copy(fontFamily = family),
    headlineLarge = headlineLarge.copy(fontFamily = family),
    headlineMedium = headlineMedium.copy(fontFamily = family),
    headlineSmall = headlineSmall.copy(fontFamily = family),
    titleLarge = titleLarge.copy(fontFamily = family),
    titleMedium = titleMedium.copy(fontFamily = family),
    titleSmall = titleSmall.copy(fontFamily = family),
    bodyLarge = bodyLarge.copy(fontFamily = family),
    bodyMedium = bodyMedium.copy(fontFamily = family),
    bodySmall = bodySmall.copy(fontFamily = family),
    labelLarge = labelLarge.copy(fontFamily = family),
    labelMedium = labelMedium.copy(fontFamily = family),
    labelSmall = labelSmall.copy(fontFamily = family),
)
