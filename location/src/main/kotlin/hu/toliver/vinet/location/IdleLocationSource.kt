package hu.toliver.vinet.location

import hu.toliver.vinet.core.model.GeoFix
import hu.toliver.vinet.core.port.LocationSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/** Live capture placeholder until [PlatformLocationSource] lands. Emits no fixes. */
class IdleLocationSource : LocationSource {
    override val fixes: Flow<GeoFix> = emptyFlow()

    override fun start() = Unit

    override fun stop() = Unit
}
