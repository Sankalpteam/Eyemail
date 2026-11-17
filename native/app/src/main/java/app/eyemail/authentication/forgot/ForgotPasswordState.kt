package app.eyemail.authentication.forgot

data class ForgotPasswordState(
    val email: String = "",
    val busy: Boolean = false,
    val error: String = "",
    val done: String = "",
) {
    val canSubmit: Boolean
        get() = !busy && email.isNotBlank()
}
