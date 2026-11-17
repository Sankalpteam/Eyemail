package app.eyemail.settings.notifications

data class NotificationsState(
    val enabled: Boolean = false,
    val permissionGranted: Boolean = false,
    val blocked: Boolean = false,
)
