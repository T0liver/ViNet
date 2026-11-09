package hu.toliver.vinet.core.model

/**
 * Axis-aligned box in 0..1 of the upright source frame after letterbox removal.
 * Origin top-left; y increases downward.
 */
data class NormalizedBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
)
