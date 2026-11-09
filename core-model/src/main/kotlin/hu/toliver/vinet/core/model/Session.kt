package hu.toliver.vinet.core.model

import hu.toliver.vinet.core.config.ConfigSnapshot

data class Session(
    val id: String,
    val startUtcMs: Long,
    val endUtcMs: Long?,
    val deviceInfo: String,
    val packId: String,
    val modelVersion: String,
    val config: ConfigSnapshot,
    val notes: String,
    val plotName: String?,
)
