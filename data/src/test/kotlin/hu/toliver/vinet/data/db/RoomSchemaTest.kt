package hu.toliver.vinet.data.db

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText

class RoomSchemaTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun exportedSchemaListsEveryColumn() {
        val schemaFile = locateExportedSchema()
        val root = json.parseToJsonElement(schemaFile.readText()).jsonObject
        val entitiesJson = root.getValue("database").jsonObject.getValue("entities").jsonArray
        val entities = entitiesJson.associate { entityElement ->
            val entity = entityElement.jsonObject
            val table = entity.getValue("tableName").jsonPrimitive.content
            val columns = entity.getValue("fields").jsonArray
                .map { field -> field.jsonObject.getValue("columnName").jsonPrimitive.content }
                .toSet()
            table to columns
        }

        val expected = mapOf(
            "sessions" to setOf(
                "id",
                "startUtcMs",
                "endUtcMs",
                "deviceInfo",
                "packId",
                "modelVersion",
                "notes",
                "plotName",
                "maxTrackAge",
                "minTrackFrames",
                "perClassMinScore",
                "mergeDistanceM",
                "mergeWindowS",
                "minSpeedMps",
                "segmentLengthM",
                "mountSide",
                "mountOffsetM",
                "maxAccuracyM",
                "weakAccuracyM",
                "bestFrameConfidence",
                "bestFrameBoxArea",
                "bestFrameFullyInside",
                "bestFrameSharpness",
            ),
            "events" to setOf(
                "id",
                "sessionId",
                "timestampUtcMs",
                "lat",
                "lon",
                "accuracyM",
                "speedMps",
                "bearingDeg",
                "classId",
                "className",
                "confidence",
                "trackId",
                "boxLeft",
                "boxTop",
                "boxRight",
                "boxBottom",
                "imagePath",
                "thumbPath",
                "plotName",
                "userFeedback",
                "packId",
                "modelVersion",
            ),
            "segments" to setOf(
                "id",
                "sessionId",
                "density",
                "severity",
            ),
        )

        assertEquals(expected.keys, entities.keys)
        expected.forEach { (table, columns) ->
            val actual = entities.getValue(table)
            val missing = columns - actual
            val extra = actual - columns
            assertTrue(
                "Table $table missing columns $missing (extra $extra)",
                missing.isEmpty() && extra.isEmpty(),
            )
        }
    }

    @Test
    fun noModulePersistsSessionsEventsOrSegments() {
        val repoRoot = locateRepoRoot()
        val kotlinSources = Files.walk(repoRoot)
            .filter { path ->
                path.isRegularFile() &&
                    (path.toString().endsWith(".kt") || path.toString().endsWith(".java"))
            }
            .filter { path ->
                val relative = repoRoot.relativize(path).toString()
                relative.contains("/src/main/") || relative.contains("/src/androidTest/")
            }
            .toList()

        val writeAnnotation = Regex("""@(Insert|Update|Delete)\b""")
        val persistedTypes = Regex(
            """\b(SessionEntity|EventEntity|SegmentEntity|Session|Event|Segment)\b""",
        )
        val violations = mutableListOf<String>()

        kotlinSources.forEach { file ->
            val lines = file.readText().lines()
            lines.forEachIndexed { index, line ->
                if (!writeAnnotation.containsMatchIn(line)) return@forEachIndexed
                val window = lines.subList(index, (index + 8).coerceAtMost(lines.size)).joinToString("\n")
                if (persistedTypes.containsMatchIn(window)) {
                    violations += "${repoRoot.relativize(file)}:${index + 1}: $line"
                }
            }
        }

        if (violations.isNotEmpty()) {
            fail(
                buildString {
                    appendLine("Story 1.4 forbids insert/update/delete of sessions, events, or segments:")
                    violations.forEach { appendLine("  $it") }
                },
            )
        }
    }

    private fun locateExportedSchema(): File {
        val moduleDir = File(System.getProperty("user.dir"))
        val candidates = listOf(
            File(moduleDir, "schemas/hu.toliver.vinet.data.db.ViNetDatabase/1.json"),
            File(moduleDir, "src/test/resources/hu.toliver.vinet.data.db.ViNetDatabase/1.json"),
        )
        for (candidate in candidates) {
            if (candidate.isFile) return candidate
        }
        throw AssertionError("Exported Room schema not found under data/schemas; run :data:kspDebugKotlin first")
    }

    private fun locateRepoRoot(): Path {
        var dir = Path.of(System.getProperty("user.dir"))
        repeat(6) {
            if (dir.resolve("settings.gradle.kts").toFile().isFile) {
                return dir
            }
            dir = dir.parent ?: return dir
        }
        return dir
    }
}
