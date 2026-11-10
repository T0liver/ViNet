package hu.toliver.vinet.core.config

import org.junit.Assert.assertEquals
import org.junit.Test

class ConfigSnapshotCameraNamesTest {
    @Test
    fun snapshotExposesExposureCapAndFocusDistanceNames() {
        assertEquals("exposure-cap", ConfigSnapshot.EXPOSURE_CAP_NAME)
        assertEquals("focus-distance", ConfigSnapshot.FOCUS_DISTANCE_NAME)
        val snapshot = ConfigSnapshot.uninitialized()
        assertEquals(0L, snapshot.exposureCapNs)
        assertEquals(0f, snapshot.focusDistance, 0f)
    }
}
