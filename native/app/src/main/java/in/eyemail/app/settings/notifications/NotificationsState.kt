package in.eyemail.app.settings.notifications

data class NotificationsState(
    val enabled: Boolean = false,
    val permissionGranted: Boolean = false,
    val blocked: Boolean = false,
)
