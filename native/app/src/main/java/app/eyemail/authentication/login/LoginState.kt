package app.eyemail.authentication.login

/** Everything the sign-in screen can show, in one value. */
data class LoginState(
    val email: String = "",
    val password: String = "",
    val busy: Boolean = false,
    val error: String = "",
) {
    val canSubmit: Boolean
        get() = !busy && email.isNotBlank() && password.length >= 6
}
