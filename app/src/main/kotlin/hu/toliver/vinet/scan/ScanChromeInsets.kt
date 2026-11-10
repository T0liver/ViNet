package hu.toliver.vinet.scan

data class ScanEdgePadding(
    val leftPx: Int = 0,
    val topPx: Int = 0,
    val rightPx: Int = 0,
    val bottomPx: Int = 0,
)

/**
 * Scan chrome clears the system bars. The camera preview stays edge to edge.
 * Screen off hides those bars, so the chrome inset drops to zero.
 */
object ScanChromeInsets {
    const val previewFillsScreen: Boolean = true

    fun chromePadding(
        leftPx: Int,
        topPx: Int,
        rightPx: Int,
        bottomPx: Int,
        screenBlanked: Boolean,
    ): ScanEdgePadding {
        if (screenBlanked) return ScanEdgePadding()
        return ScanEdgePadding(
            leftPx = leftPx.coerceAtLeast(0),
            topPx = topPx.coerceAtLeast(0),
            rightPx = rightPx.coerceAtLeast(0),
            bottomPx = bottomPx.coerceAtLeast(0),
        )
    }
}
