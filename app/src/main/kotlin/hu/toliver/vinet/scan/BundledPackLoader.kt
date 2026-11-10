package hu.toliver.vinet.scan

import android.content.Context
import hu.toliver.vinet.core.pack.ModelPack
import hu.toliver.vinet.core.pack.PackManifestParser

object BundledPackLoader {
    private const val MANIFEST_ASSET = "scan-scripted-manifest.json"

    fun loadScanPack(context: Context): ModelPack {
        val json = context.assets.open(MANIFEST_ASSET).bufferedReader().use { it.readText() }
        return PackManifestParser.parse(json)
    }
}
