package app.eyemail.authentication.signup

/** The form, then the code. Both steps live in this one state. */
data class SignUpState(
    val name: String = "",
    val username: String = "",
    val recovery: String = "",
    val password: String = "",
    val otp: String = "",
    val step: Step = Step.Form,
    val pendingEmail: String = "",
    val pendingMasked: String = "",
    val pendingToken: String = "",
    val busy: Boolean = false,
    val error: String = "",
    val note: String = "",
) {
    enum class Step { Form, Code }

    val canCreate: Boolean
        get() = !busy && name.isNotBlank() && username.isNotBlank() &&
            recovery.contains("@") && password.length >= 6

    val canVerify: Boolean
        get() = !busy && otp.length == 6
}
