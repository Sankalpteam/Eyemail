package app.eyemail.settings

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import app.eyemail.Ey

/**
 * One settings page, loaded from the web.
 *
 * Nothing is bundled, so the text on the phone is always the text on the site. The arrow
 * goes back to the list, and the page itself keeps its own scroll position while it is open.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SettingsWebPage(
    url: String,
    title: String,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(Ey.Bg).statusBarsPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Ey.Surface)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(34.dp).clickable { onBack() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to settings",
                    tint = Ey.Text,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ey.Text)
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    // The pages keep your choices on the device, which needs a little
                    // script. They are our own pages on our own address, and the loader
                    // refuses to leave that address.
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: android.webkit.WebResourceRequest?,
                        ): Boolean {
                            val target = request?.url?.toString().orEmpty()
                            // Stay on eyemail.in. Anything else opens nowhere, rather than
                            // loading an unknown page inside the app.
                            return !target.startsWith("https://eyemail.in/")
                        }
                    }
                    loadUrl(url)
                }
            },
            update = { view ->
                if (view.url != url) view.loadUrl(url)
            },
        )
    }
}
