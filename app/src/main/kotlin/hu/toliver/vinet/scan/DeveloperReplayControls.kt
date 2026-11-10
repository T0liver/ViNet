package hu.toliver.vinet.scan

interface DeveloperReplayControls {
    val developerToolsEnabled: Boolean

    fun toggleDeveloperTools()

    fun selectVideoReplay(videoPath: String, gpxPath: String)

    fun selectLiveCamera()
}

object OffDeveloperReplayControls : DeveloperReplayControls {
    override val developerToolsEnabled: Boolean = false

    override fun toggleDeveloperTools() = Unit

    override fun selectVideoReplay(videoPath: String, gpxPath: String) = Unit

    override fun selectLiveCamera() = Unit
}
