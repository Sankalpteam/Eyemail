package app.eyemail.settings.account

data class AccountState(
    val displayName: String = "",
    val email: String = "",
    val recoveryEmail: String = "",
    val sendingReset: Boolean = false,
    val resetNote: String = "",
    val resetError: String = "",
    val signingOutAll: Boolean = false,
    val deleting: Boolean = false,
    val actionNote: String = "",
    val actionError: String = "",
)
