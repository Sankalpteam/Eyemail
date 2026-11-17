package in.eyemail.app.settings.storage

data class StorageState(
    val usedBytes: Long = 0,
    val limitBytes: Long = 1L shl 30,
) {
    val usedMb: Long get() = usedBytes / (1024 * 1024)
    val limitGb: Long get() = (limitBytes / (1024 * 1024 * 1024)).coerceAtLeast(1)
    val percent: Float get() = if (limitBytes > 0) (usedBytes.toFloat() / limitBytes) else 0f
}
