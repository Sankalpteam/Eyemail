package app.eyemail.settings

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.eyemail.Ey
import app.eyemail.settings.account.AccountSection
import app.eyemail.settings.about.AboutSection
import app.eyemail.settings.appearance.AppearanceSection
import app.eyemail.settings.mail.MailSection
import app.eyemail.settings.notifications.NotificationsSection
import app.eyemail.settings.storage.StorageSection

/**
 * Settings, full screen on a phone, with the six sections as tabs across the top
 * and each section in its own folder behind them.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    onSignedOutEverywhere: () -> Unit,
    viewModel: SettingsViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SettingsViewModel(onSignedOutEverywhere) }
        },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // A legal page opened from the list. Null shows the list.
    var webLegal by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf<Pair<String, String>?>(null)
    }



    Box(modifier = Modifier.fillMaxSize().background(Ey.Bg)) {
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Ey.Surface)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val open = state.open
            val legalOpen = webLegal
            val showing = open?.label ?: legalOpen?.second
            if (showing != null) {
                // An arrow, not the word back, and it returns to the list where it was.
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clickable {
                            if (state.open != null) viewModel.closeSection() else webLegal = null
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to settings",
                        tint = Ey.Text,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(showing, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ey.Text)
            } else {
                Text("Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ey.Text)
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onBack) {
                Text("Close", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Ey.Accent)
            }
        }


        if (state.error.isNotBlank()) {
            Text(
                state.error,
                fontSize = 11.5.sp,
                color = Ey.Danger,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Ey.DangerSoft)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }

        // Each section keeps its own scroll position, and opening a section starts at the top.
        val scroll = rememberScrollState()
        LaunchedEffect(state.section) { scroll.scrollTo(0) }

        // A page is drawn on its own, never inside the scrolling list: a web view inside a
        // scrolling column gets no height of its own and shows nothing.
        val legalPage = webLegal
        val sectionPage = state.open
        if (legalPage != null || sectionPage != null) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            SettingsWebPage(
                url = if (legalPage != null) {
                    legalPage.first + "?inapp=1"
                } else {
                    SettingsWeb.forSection(sectionPage!!.name.lowercase()) + "?inapp=1"
                },
                title = legalPage?.second ?: sectionPage!!.label,
                onBack = {
                    if (legalPage != null) webLegal = null else viewModel.closeSection()
                },
            )
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scroll),
        ) {
            if (state.open == null) {
                // One card, six rows, exactly the shape the web shows: a title, a small
                // label under it, one fact on the right, and an arrow. No grey band per
                // section, and no row taller than its own text.
                Spacer(Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .background(Ey.Surface, RoundedCornerShape(14.dp)),
                ) {
                    SettingsState.Section.entries.forEachIndexed { index, section ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.open(section) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    sectionRowTitle(section),
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Ey.Text,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    section.label.uppercase(),
                                    fontSize = 10.sp,
                                    letterSpacing = 0.6.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Ey.Text2,
                                )
                            }
                            Text(
                                sectionRowSummary(section, state),
                                fontSize = 12.sp,
                                color = Ey.Text2,
                                maxLines = 1,
                            )
                            Spacer(Modifier.width(6.dp))
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Ey.Text3,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        if (index != SettingsState.Section.entries.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Ey.Border),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "LEGAL",
                    fontSize = 10.sp,
                    letterSpacing = 0.6.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ey.Text2,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Ey.Surface, RoundedCornerShape(14.dp)),
                ) {
                    listOf(
                        "Terms of Use" to app.eyemail.settings.terms_of_use.TermsOfUsePage.URL,
                        "Privacy Policy" to app.eyemail.settings.privacy_policy.PrivacyPolicyPage.URL,
                        "Security" to app.eyemail.settings.security.SecurityPage.URL,
                        "Data and Privacy" to app.eyemail.settings.data_and_privacy.DataAndPrivacyPage.URL,
                    ).forEachIndexed { index, (name, url) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { webLegal = url to name }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                name,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Ey.Text,
                                modifier = Modifier.weight(1f),
                            )
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Ey.Text3,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        if (index != 3) {
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Ey.Border))
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    "POWERED BY INDO WORKSPACE",
                    fontSize = 9.5.sp,
                    letterSpacing = 1.1.sp,
                    color = Ey.Text3,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
                return@Column
            }

            when (state.section) {
                SettingsState.Section.Account -> AccountSection(
                    state = state.account,
                    onName = viewModel::setName,
                    onSendReset = viewModel::sendResetLink,
                    onSignOutAll = viewModel::signOutEverywhere,
                    onDelete = { viewModel.deleteAccount(onAccountDeleted) },
                )

                SettingsState.Section.Appearance -> AppearanceSection(
                    state = state.appearance,
                    onTheme = viewModel::setTheme,
                    onDensity = viewModel::setDensity,
                )

                SettingsState.Section.Mail -> MailSection(
                    state = state.mail,
                    onSignature = viewModel::setSignature,
                    onUndo = viewModel::setUndo,
                    onConfirmDelete = viewModel::setConfirmDelete,
                )

                SettingsState.Section.Notifications -> NotificationsSection(
                    state = state.notifications,
                    onChange = viewModel::setNotifications,
                    onTest = viewModel::testPush,
                    onCheckLast = viewModel::checkLastPush,
                    onRefresh = viewModel::refreshNotifications,
                )

                SettingsState.Section.Storage -> StorageSection(state = state.storage)

                SettingsState.Section.About -> AboutSection(state = state.about)
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "POWERED BY INDO WORKSPACE",
                fontSize = 9.5.sp,
                letterSpacing = 1.1.sp,
                color = Ey.Text3,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }

    }
}

/** The row title: what the section is called, in the words a person uses. */
private fun sectionRowTitle(section: SettingsState.Section): String = when (section) {
    SettingsState.Section.Account -> "Name, address and password"
    SettingsState.Section.Appearance -> "Theme and row height"
    SettingsState.Section.Mail -> "Signature, undo and delete"
    SettingsState.Section.Notifications -> "Mail alerts on this phone"
    SettingsState.Section.Storage -> "Space used and free"
    SettingsState.Section.About -> "Version, build and the company"
}

/** The row summary: the one fact worth seeing without opening it. */
private fun sectionRowSummary(section: SettingsState.Section, state: SettingsState): String = when (section) {
    SettingsState.Section.Account -> state.account.email.ifBlank { "" }
    SettingsState.Section.Appearance -> state.appearance.theme.lowercase()
    SettingsState.Section.Mail -> if (state.mail.signature.isBlank()) "no signature" else "signature set"
    SettingsState.Section.Notifications -> if (state.notifications.enabled) "on" else "off"
    SettingsState.Section.Storage -> {
        val used = (state.storage.usedBytes / (1024.0 * 1024.0))
        String.format("%.1f MB used", used)
    }
    SettingsState.Section.About -> state.about.version
}

// ---------------------------------------------------------------------------------------
// The page loader and the addresses it needs. They live here because this is the only
// screen that opens a settings page, and a folder of two files used by one screen is one
// thing too many.
// ---------------------------------------------------------------------------------------

private object SettingsWeb {
    private const val BASE = "https://sankalpteam.github.io/Eyemail/web/settings/"

    fun forSection(section: String): String = when (section) {
        "account" -> "${BASE}account.html"
        "appearance" -> "${BASE}appearance.html"
        "mail" -> "${BASE}mail.html"
        "notifications" -> "${BASE}notifications.html"
        "storage" -> "${BASE}storage.html"
        "about" -> "${BASE}about.html"
        else -> BASE
    }
}

/**
 * One settings page, loaded from the web.
 *
 * Nothing is bundled, so the text on the phone is always the text on the site. The arrow
 * goes back to the list, and the page itself keeps its own scroll position while it is open.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun SettingsWebPage(
    url: String,
    title: String,
    onBack: () -> Unit,
) {
    var failed by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var attempt by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
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

        if (failed) {
            // A page that cannot be reached is ours to explain, not the browser's.
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(36.dp))
                Text(
                    "This page could not be reached",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ey.Text,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Check your internet connection, then try again.",
                    fontSize = 12.5.sp,
                    color = Ey.Text2,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    "Try again",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ey.Accent,
                    modifier = Modifier.clickable {
                        failed = false
                        attempt += 1
                    },
                )
            }
            return@Column
        }

        androidx.compose.runtime.key(attempt) {
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
                        override fun onPageFinished(view: WebView?, address: String?) {
                            // The phone draws the header, so the page's own copy is removed
                            // once it has loaded. Doing it here cannot be skipped by a cache.
                            view?.evaluateJavascript(
                                "(function(){var t=document.querySelector('.top');if(t){t.remove();}var b=document.body;if(b){b.style.paddingTop='0px';}})();",
                                null,
                            )
                        }
                        override fun onReceivedError(
                            view: WebView?,
                            request: android.webkit.WebResourceRequest?,
                            error: android.webkit.WebResourceError?,
                        ) {
                            // Only a failure of the page itself matters; a picture or a font
                            // failing is not worth a whole screen.
                            if (request?.isForMainFrame == true) failed = true
                        }
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
}
