package hu.toliver.vinet.scan

/**
 * Foreground-service notification content for live scan and replay sessions.
 * Screen-off tractor mode uses [SCREEN_OFF_NOTIFICATION_TEXT].
 */
object ScanSessionNotification {
    const val SCREEN_OFF_NOTIFICATION_TEXT = "Screen off. Scanning continues."
    const val STOP_ACTION_INTENT = ScanForegroundService.ACTION_STOP

    data class Content(
        val bodyText: String,
        val includesStopAction: Boolean,
    )

    fun content(
        sessionRunning: Boolean,
        tractorMode: TractorDisplayMode,
        pressureSentence: String? = null,
    ): Content {
        if (!sessionRunning) {
            return Content(bodyText = "", includesStopAction = false)
        }
        if (pressureSentence != null) {
            return Content(bodyText = pressureSentence, includesStopAction = true)
        }
        return when (tractorMode) {
            TractorDisplayMode.ScreenOff ->
                Content(
                    bodyText = SCREEN_OFF_NOTIFICATION_TEXT,
                    includesStopAction = true,
                )
            TractorDisplayMode.DayScreen,
            TractorDisplayMode.Dim,
            ->
                Content(
                    bodyText = RUNNING_NOTIFICATION_TEXT,
                    includesStopAction = false,
                )
        }
    }

    const val RUNNING_NOTIFICATION_TEXT = "Scan is running"
}
