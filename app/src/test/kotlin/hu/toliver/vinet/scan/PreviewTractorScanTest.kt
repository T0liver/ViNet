package hu.toliver.vinet.scan

import hu.toliver.vinet.core.overlay.OverlayBox
import hu.toliver.vinet.core.model.Detection
import hu.toliver.vinet.core.model.NormalizedBox
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewTractorScanTest {
    private val sampleBox =
        OverlayBox(
            className = "Scripted grape leaf",
            colorHex = "#4B0082",
            detection =
                Detection(
                    classId = 0,
                    score = 0.9f,
                    box = NormalizedBox(0.1f, 0.1f, 0.2f, 0.2f),
                    frameTimestampNs = 1L,
                ),
        )

    @Test
    fun hidingPreviewHidesOverlayWhileSessionStaysRunning() {
        val repo = ScanStateRepository()
        repo.onServiceRunning(listOf(sampleBox))
        repo.setPreviewVisible(false)

        val state = repo.state.value
        assertTrue(state.isRunning)
        assertEquals("Running", state.statusLabel)
        assertEquals(1, state.overlayBoxes.size)
        assertFalse(ScanPreviewControl.showDetectionOverlay(state.previewVisible))
        assertFalse(ScanPreviewControl.showCameraPreview(state.previewVisible, state.tractorMode))
    }

    @Test
    fun tractorControlCyclesDayDimAndScreenOff() {
        var mode = TractorDisplayMode.DayScreen
        mode = mode.next()
        assertEquals(TractorDisplayMode.Dim, mode)
        mode = mode.next()
        assertEquals(TractorDisplayMode.ScreenOff, mode)
        mode = mode.next()
        assertEquals(TractorDisplayMode.DayScreen, mode)

        val repo = ScanStateRepository()
        repo.onServiceRunning(emptyList())
        assertEquals(TractorDisplayMode.DayScreen, repo.state.value.tractorMode)
        repo.cycleTractorMode()
        assertEquals(TractorDisplayMode.Dim, repo.state.value.tractorMode)
        repo.cycleTractorMode()
        assertEquals(TractorDisplayMode.ScreenOff, repo.state.value.tractorMode)
        repo.cycleTractorMode()
        assertEquals(TractorDisplayMode.DayScreen, repo.state.value.tractorMode)
        assertTrue(repo.state.value.isRunning)
    }

    @Test
    fun screenOffNotificationTextIsExact() {
        val content =
            ScanSessionNotification.content(
                sessionRunning = true,
                tractorMode = TractorDisplayMode.ScreenOff,
            )
        assertEquals("Screen off. Scanning continues.", content.bodyText)
    }

    @Test
    fun screenOffNotificationIncludesStopActionForService() {
        val content =
            ScanSessionNotification.content(
                sessionRunning = true,
                tractorMode = TractorDisplayMode.ScreenOff,
            )
        assertTrue(content.includesStopAction)
        assertEquals(ScanForegroundService.ACTION_STOP, ScanSessionNotification.STOP_ACTION_INTENT)
    }

    @Test
    fun dayAndDimNotificationsStayOnRunningCopyWithoutStopAction() {
        val day =
            ScanSessionNotification.content(
                sessionRunning = true,
                tractorMode = TractorDisplayMode.DayScreen,
            )
        val dim =
            ScanSessionNotification.content(
                sessionRunning = true,
                tractorMode = TractorDisplayMode.Dim,
            )
        assertEquals(ScanSessionNotification.RUNNING_NOTIFICATION_TEXT, day.bodyText)
        assertEquals(ScanSessionNotification.RUNNING_NOTIFICATION_TEXT, dim.bodyText)
        assertFalse(day.includesStopAction)
        assertFalse(dim.includesStopAction)
    }

    @Test
    fun dayAndDimKeepControlsClearOfTheSystemBars() {
        val padding =
            ScanChromeInsets.chromePadding(
                leftPx = 0,
                topPx = 80,
                rightPx = 0,
                bottomPx = 140,
                screenBlanked = false,
            )
        assertEquals(80, padding.topPx)
        assertEquals(140, padding.bottomPx)
        assertTrue(ScanChromeInsets.previewFillsScreen)
    }

    @Test
    fun screenOffDropsTheSystemBarInset() {
        val padding =
            ScanChromeInsets.chromePadding(
                leftPx = 24,
                topPx = 80,
                rightPx = 24,
                bottomPx = 140,
                screenBlanked = true,
            )
        assertEquals(0, padding.leftPx)
        assertEquals(0, padding.topPx)
        assertEquals(0, padding.rightPx)
        assertEquals(0, padding.bottomPx)
    }

    @Test
    fun screenOffBlanksScanChromeWhileDimOnlyScrims() {
        assertTrue(ScanPreviewControl.blankScreenForTractor(TractorDisplayMode.ScreenOff))
        assertFalse(ScanPreviewControl.blankScreenForTractor(TractorDisplayMode.Dim))
        assertTrue(ScanPreviewControl.applyDimScrim(TractorDisplayMode.Dim))
        assertFalse(
            ScanPreviewControl.showCameraPreview(
                previewVisible = true,
                tractorMode = TractorDisplayMode.ScreenOff,
            ),
        )
    }
}
