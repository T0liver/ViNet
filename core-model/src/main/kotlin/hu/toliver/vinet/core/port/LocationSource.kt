package hu.toliver.vinet.core.port

import hu.toliver.vinet.core.model.GeoFix
import kotlinx.coroutines.flow.Flow

interface LocationSource {
    val fixes: Flow<GeoFix>

    fun start()

    fun stop()
}
