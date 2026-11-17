package app.eyemail.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * The theme has to be readable as state, or changing it in Settings does nothing
 * until the app restarts. This holds the value the whole app watches.
 */
object ThemeState {
    var dark by mutableStateOf(false)
        private set

    fun load() {
        dark = SettingsStore.theme() == "dark"
    }

    fun set(value: String) {
        SettingsStore.setTheme(value)
        dark = value == "dark"
    }
}
