package app.eyemail.settings.appearance

data class AppearanceState(
    val theme: String = "light",       // light | dark
    val density: String = "comfortable", // comfortable | compact
)
