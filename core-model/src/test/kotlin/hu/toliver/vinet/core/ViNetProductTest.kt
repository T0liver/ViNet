package hu.toliver.vinet.core

import org.junit.Assert.assertEquals
import org.junit.Test

class ViNetProductTest {
    @Test
    fun launcherWordmarkMatchesProductName() {
        assertEquals("ViNet", ViNetProduct.DISPLAY_NAME)
    }
}
