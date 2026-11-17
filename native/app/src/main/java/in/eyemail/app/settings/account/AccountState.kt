package in.eyemail.app.settings.account

data class AccountState(
    val displayName: String = "",
    val email: String = "",
    val recoveryEmail: String = "",
    val sendingReset: Boolean = false,
    val resetNote: String = "",
    val resetError: String = "",
)
