package app.eyemail.settings.mail

data class MailState(
    val signature: String = "",
    val undoSeconds: Int = 5,
    val confirmDelete: Boolean = true,
)
