package hu.toliver.vinet.core.pack

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText

class PackManifestParserTest {
    @Test
    fun classNameAndColorComeFromManifestFileNotFromCode() {
        val manifestPath = resourcePath("sample-manifest.json")
        val manifestJson = manifestPath.readText(StandardCharsets.UTF_8)

        val expectedName = Regex(""""name"\s*:\s*"([^"]+)"""")
            .findAll(manifestJson)
            .map { it.groupValues[1] }
            .first()
        val expectedColor = Regex(""""color"\s*:\s*"([^"]+)"""")
            .findAll(manifestJson)
            .map { it.groupValues[1] }
            .first()

        val pack = PackManifestParser.parse(manifestJson)
        val first = pack.classes.first()

        assertEquals(expectedName, first.name)
        assertEquals(expectedColor, first.color)
        assertEquals(0.42f, first.minScore, 0.0001f)

        val mainSources = projectCoreModelMainSources()
        mainSources.forEach { sourceFile ->
            val text = sourceFile.readText(StandardCharsets.UTF_8)
            assertFalse(
                "Production code must not hard-code manifest class name: $sourceFile",
                text.contains(expectedName),
            )
            assertFalse(
                "Production code must not hard-code manifest class color: $sourceFile",
                text.contains(expectedColor),
            )
        }
    }

    @Test
    fun preprocessingFieldsReadFromManifestOnly() {
        val manifestJson = resourcePath("sample-manifest.json").readText(StandardCharsets.UTF_8)
        val pack = PackManifestParser.parse(manifestJson)

        assertTrue(pack.inputFormat.letterbox)
        assertEquals(114, pack.inputFormat.padValue)
        assertEquals("RGB", pack.inputFormat.colorOrder)
        assertEquals(640, pack.inputWidth)
        assertEquals(640, pack.inputHeight)
    }

    private fun resourcePath(name: String): Path {
        val url = checkNotNull(javaClass.getResource("/$name")) { "missing test resource $name" }
        return Path.of(url.toURI())
    }

    private fun projectCoreModelMainSources(): List<Path> {
        val cwd = Path.of(System.getProperty("user.dir"))
        val moduleDir = if (cwd.fileName.toString() == "core-model") {
            cwd.resolve("src/main/kotlin")
        } else {
            cwd.resolve("core-model/src/main/kotlin")
        }
        check(Files.isDirectory(moduleDir)) { "expected core-model sources at $moduleDir" }
        return Files.walk(moduleDir)
            .filter { it.toString().endsWith(".kt") }
            .toList()
    }
}
