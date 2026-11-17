package in.eyemail.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import in.eyemail.app.ui.theme.Ey

/** The envelope mark, drawn so the app carries no image assets it does not need. */
@Composable
fun EyemailMark(size: Int = 40, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size.dp)
            .background(
                Brush.linearGradient(listOf(Ey.Accent, Ey.Accent2)),
                RoundedCornerShape((size * 0.28f).dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size((size * 0.52f).dp)) {
            val w = this.size.width
            val h = this.size.height
            val stroke = Stroke(width = w * 0.10f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            val rect = Size(w, h * 0.72f)
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(0f, (h - rect.height) / 2f),
                size = rect,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.14f),
                style = stroke,
            )
            val flap = Path().apply {
                moveTo(w * 0.06f, (h - rect.height) / 2f + h * 0.10f)
                lineTo(w * 0.50f, h * 0.58f)
                lineTo(w * 0.94f, (h - rect.height) / 2f + h * 0.10f)
            }
            drawPath(flap, Color.White, style = stroke)
        }
    }
}

/** Title block used at the top of every auth screen: mark, name, tagline. */
@Composable
fun BrandHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EyemailMark(size = 40)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Eyemail", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Ey.Text)
                Text(
                    "EYEMAIL.IN",
                    fontSize = 9.5.sp,
                    letterSpacing = 1.4.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ey.Text3,
                )
            }
        }
        Spacer(Modifier.height(26.dp))
        Text(title, style = MaterialTheme.typography.displaySmall, color = Ey.Text)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Ey.Text2)
    }
}

@Composable
fun EyField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isPassword: Boolean = false,
    enabled: Boolean = true,
) {
    var revealed by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ey.Text2,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            placeholder = { Text(placeholder, color = Ey.Text3, fontSize = 14.sp) },
            visualTransformation = if (isPassword && !revealed) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            trailingIcon = if (isPassword) {
                {
                    TextButton(onClick = { revealed = !revealed }) {
                        Text(if (revealed) "Hide" else "Show", fontSize = 12.sp, color = Ey.Accent)
                    }
                }
            } else {
                null
            },
            shape = RoundedCornerShape(11.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Ey.Accent,
                unfocusedBorderColor = Ey.BorderStrong,
                focusedContainerColor = Ey.Surface,
                unfocusedContainerColor = Ey.Surface,
                focusedTextColor = Ey.Text,
                unfocusedTextColor = Ey.Text,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        if (hint != null) {
            Text(
                hint,
                fontSize = 11.sp,
                color = Ey.Text3,
                modifier = Modifier.padding(top = 5.dp),
            )
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    enabled: Boolean = true,
) {
    val active = enabled && !loading
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(
                if (active) Brush.linearGradient(listOf(Ey.Accent, Ey.Accent2))
                else Brush.linearGradient(listOf(Ey.BorderStrong, Ey.BorderStrong)),
                RoundedCornerShape(12.dp),
            )
            .then(if (active) Modifier.clickableNoRipple(onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            )
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Ey.Surface, RoundedCornerShape(12.dp))
            .border(1.dp, Ey.BorderStrong, RoundedCornerShape(12.dp))
            .clickableNoRipple(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Ey.Text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Alert(text: String, isError: Boolean, modifier: Modifier = Modifier) {
    if (text.isBlank()) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isError) Color(0xFFFEE4E2) else Color(0xFFE7F6EC),
                RoundedCornerShape(10.dp),
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text,
            fontSize = 12.5.sp,
            color = if (isError) Ey.Danger else Ey.Ok,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun LinkRow(
    prompt: String,
    action: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(prompt, fontSize = 12.5.sp, color = Ey.Text3)
        TextButton(onClick = onClick) {
            Text(action, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Ey.Accent)
        }
    }
}

@Composable
fun FooterCredit(modifier: Modifier = Modifier) {
    Text(
        "POWERED BY INDO WORKSPACE",
        fontSize = 10.sp,
        letterSpacing = 1.2.sp,
        fontWeight = FontWeight.Medium,
        color = Ey.Text3,
        modifier = modifier.fillMaxWidth(),
    )
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.then(Modifier.clickable(onClick = onClick))
