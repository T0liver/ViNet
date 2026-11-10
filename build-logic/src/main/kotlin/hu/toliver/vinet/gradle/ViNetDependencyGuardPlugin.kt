package hu.toliver.vinet.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import java.io.File
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.ZipFile

private val PURE_MODULES = listOf(":core-model", ":tracking", ":density", ":export")
private val PORT_MODULES = listOf(":inference", ":camera", ":location", ":data")

private val ANDROID_IMPORT = Regex("""^\s*import\s+(android|androidx)\.[^\s]+""")

private val PLAY_SERVICES_GROUP_PREFIXES = listOf(
    "com.google.android.gms",
    "com.google.firebase",
    "com.google.android.play",
    "com.google.mlkit",
    "com.google.android.libraries.maps",
)

private val PLAY_SERVICES_MODULE_HINTS = listOf(
    "play-services",
    "play-services-",
    "firebase-",
    "mlkit",
    "maps-sdk",
    "tflite-java",
    "tflite-gpu",
)

private val MODEL_FILE_EXTENSIONS = setOf(
    ".tflite",
    ".onnx",
    ".ort",
    ".pte",
    ".ncnn",
    ".mlmodelc",
)

private val REPLAY_MEDIA_EXTENSIONS = setOf(
    ".mp4",
    ".mov",
    ".mkv",
    ".webm",
    ".gpx",
    ".3gp",
)

private val RELEASE_FORBIDDEN_MARKERS = listOf(
    "hu/toliver/vinet/inference/parity/StandInParityCheck",
    "hu/toliver/vinet/devtools/ParityHarnessHook",
    "hu/toliver/vinet/devtools/DebugHudFormatter",
    "hu/toliver/vinet/devtools/DebugHudSampler",
    "hu/toliver/vinet/scan/DebugReplaySessionPorts",
    "Developer tools on",
    "Use video + GPX replay",
)

class ViNetDependencyGuardPlugin : Plugin<Project> {
    override fun apply(root: Project) {
        require(root.path == ":") {
            "vinet.dependency-guard must be applied to the root project"
        }

        PURE_MODULES.forEach { path ->
            root.tasks.register<ScanPureModuleSourcesTask>("scanPureModuleSources${taskSuffix(path)}") {
                group = "verification"
                description = "Reject Android imports in $path"
                moduleProjectPath.set(path)
                moduleProjectDir.set(root.layout.projectDirectory.dir("${path.removePrefix(":")}/src"))
            }
        }

        root.tasks.register<CheckGradleDependencyRulesTask>("checkGradleDependencyRules") {
            group = "verification"
            description = "Enforce AD-2 module edges, Hilt placement, and devtools wiring"
            rootDir.set(root.layout.projectDirectory)
        }

        root.tasks.register<CheckPlayServicesTask>("checkAppPlayServices") {
            group = "verification"
            description = "Reject Play services on :app releaseRuntimeClasspath"
            dependsOn(":app:assembleRelease")
            appProject.set(root.project(":app"))
        }

        root.tasks.register<CheckReleaseApkModelFilesTask>("checkReleaseApkHasNoModelFile") {
            group = "verification"
            description = "Release APK must not bundle a model, replay media, the debug HUD, or parity hooks"
            dependsOn(":app:assembleRelease")
            apkFile.set(root.layout.projectDirectory.file("app/build/outputs/apk/release/app-release-unsigned.apk"))
            reportFile.set(root.layout.buildDirectory.file("reports/dependency-guard/release-apk-model-files.txt"))
        }

        root.tasks.register("checkDependencyRules") {
            group = "verification"
            description = "Story 1.2 dependency guard (AD-2, AD-9)"
            dependsOn(
                "checkGradleDependencyRules",
                "checkAppPlayServices",
                "checkReleaseApkHasNoModelFile",
            )
            PURE_MODULES.forEach { path ->
                dependsOn("scanPureModuleSources${taskSuffix(path)}")
            }
        }

        if (root.tasks.findByName("check") == null) {
            root.tasks.register("check")
        }
        root.tasks.named("check") {
            dependsOn("checkDependencyRules")
        }

        PURE_MODULES.forEach { path ->
            val scanTask = "scanPureModuleSources${taskSuffix(path)}"
            root.project(path).afterEvaluate {
                tasks.named("check") {
                    dependsOn(root.tasks.named(scanTask))
                }
            }
        }
    }

    private fun taskSuffix(projectPath: String): String =
        projectPath.removePrefix(":").split("-").joinToString("") { part ->
            part.replaceFirstChar { c -> c.uppercase() }
        }
}

abstract class ScanPureModuleSourcesTask : DefaultTask() {
    @get:Input
    abstract val moduleProjectPath: Property<String>

    @get:InputDirectory
    abstract val moduleProjectDir: DirectoryProperty

    @TaskAction
    fun scan() {
        val moduleDir = moduleProjectDir.get().asFile
        val violations = mutableListOf<String>()
        if (moduleDir.isDirectory) {
            Files.walkFileTree(
                moduleDir.toPath(),
                object : SimpleFileVisitor<Path>() {
                    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                        val name = file.fileName.toString()
                        if (name.endsWith(".kt") || name.endsWith(".java")) {
                            file.toFile().readLines().forEachIndexed { index, line ->
                                if (ANDROID_IMPORT.containsMatchIn(line)) {
                                    val relative = moduleDir.toPath().relativize(file).toString()
                                    violations += "${moduleProjectPath.get()}:$relative:${index + 1}: $line"
                                }
                            }
                        }
                        return FileVisitResult.CONTINUE
                    }
                },
            )
        }
        if (violations.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("${moduleProjectPath.get()} must not import Android APIs (AD-2).")
                    violations.forEach { appendLine("  $it") }
                },
            )
        }
    }
}

abstract class CheckGradleDependencyRulesTask : DefaultTask() {
    @get:Internal
    abstract val rootDir: DirectoryProperty

    @TaskAction
    fun check() {
        val violations = mutableListOf<String>()
        val projects = loadSettingsIncludes(rootDir.get().asFile)

        fun projectBuild(path: String): File =
            rootDir.get().asFile.resolve(path.removePrefix(":")).resolve("build.gradle.kts")

        PURE_MODULES.forEach { path ->
            val build = projectBuild(path).readText()
            if (build.contains("com.android") || build.contains("libs.plugins.android")) {
                violations += "$path must remain a pure JVM module (kotlin-jvm only)"
            }
            if (build.contains("hilt") || build.contains("dagger")) {
                violations += "$path must not use Hilt (AD-2)"
            }
            forbiddenProjectDeps(path, build, allowed = setOf(":core-model")).forEach { violations += it }
        }

        PORT_MODULES.forEach { path ->
            val build = projectBuild(path).readText()
            forbiddenProjectDeps(path, build, allowed = setOf(":core-model")).forEach { violations += it }
            if (build.contains("project(\":devtools\")")) {
                violations += "$path must not depend on :devtools (AD-2)"
            }
        }

        val appBuild = projectBuild(":app").readText()
        if (!appBuild.contains("debugImplementation(project(\":devtools\"))")) {
            violations += ":app must use debugImplementation for :devtools"
        }
        if (appBuild.contains("implementation(project(\":devtools\"))")) {
            violations += ":app must not pull :devtools into release via implementation"
        }

        projects.forEach { path ->
            if (path != ":devtools" && path != ":app") {
                val build = projectBuild(path).readText()
                if (build.contains("project(\":devtools\")")) {
                    violations += "$path must not depend on :devtools"
                }
            }
        }

        if (violations.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("Gradle dependency rules failed:")
                    violations.forEach { appendLine("  - $it") }
                },
            )
        }
    }

    private fun forbiddenProjectDeps(
        modulePath: String,
        buildScriptText: String,
        allowed: Set<String>,
    ): List<String> {
        val deps = Regex("""project\("(:[^"]+)"\)""")
            .findAll(buildScriptText)
            .map { it.groupValues[1] }
            .toSet()
        return deps
            .filterNot { it in allowed }
            .map { dep -> "$modulePath must not depend on $dep (AD-2)" }
    }

    private fun loadSettingsIncludes(settingsDir: File): List<String> {
        val settingsFile = settingsDir.resolve("settings.gradle.kts")
        if (!settingsFile.isFile) return emptyList()
        return Regex("""":([^"]+)"""")
            .findAll(settingsFile.readText())
            .map { ":${it.groupValues[1]}" }
            .distinct()
            .toList()
    }
}

abstract class CheckPlayServicesTask : DefaultTask() {
    @get:Input
    abstract val appProject: Property<Project>

    @TaskAction
    fun check() {
        val app = appProject.get()
        val config = app.configurations.getByName("releaseRuntimeClasspath")
        val hits = mutableListOf<String>()
        config.incoming.resolutionResult.allComponents.forEach { component ->
            val id = component.id
            if (id is ModuleComponentIdentifier) {
                if (isPlayServicesArtifact(id.group, id.module)) {
                    hits += "${id.group}:${id.module}"
                }
            }
        }
        if (hits.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine(":app releaseRuntimeClasspath must not include Play services (AD-9):")
                    hits.sorted().forEach { appendLine("  - $it") }
                },
            )
        }
    }

    private fun isPlayServicesArtifact(group: String, module: String): Boolean {
        if (PLAY_SERVICES_GROUP_PREFIXES.any { group == it || group.startsWith("$it.") }) {
            return true
        }
        return PLAY_SERVICES_MODULE_HINTS.any { hint ->
            module.contains(hint, ignoreCase = true)
        }
    }
}

abstract class CheckReleaseApkModelFilesTask : DefaultTask() {
    @get:InputFile
    abstract val apkFile: RegularFileProperty

    @get:OutputFile
    @get:Optional
    abstract val reportFile: RegularFileProperty

    @TaskAction
    fun check() {
        val apk = apkFile.get().asFile
        if (!apk.isFile) {
            throw GradleException("Release APK not found at ${apk.absolutePath}; run :app:assembleRelease first")
        }
        val violations = mutableListOf<String>()
        ZipFile(apk).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                if (entry.isDirectory) return@forEach
                val name = entry.name
                val lower = name.lowercase()
                if (lower.startsWith("assets/") || lower.startsWith("res/raw/")) {
                    val extension = lower.substringAfterLast('.', "")
                    val dotted = if (extension.isNotEmpty()) ".$extension" else ""
                    val modelFile = MODEL_FILE_EXTENSIONS.contains(dotted)
                    val replayMedia = REPLAY_MEDIA_EXTENSIONS.contains(dotted)
                    val standInPack = lower.contains("stand-in/")
                    val modelPath = lower.contains("/models/") || lower.endsWith("/model.pack")
                    if (modelFile || standInPack || modelPath) {
                        violations += "MODEL: $name"
                    }
                    if (replayMedia) {
                        violations += "REPLAY: $name"
                    }
                }
                if (lower.endsWith(".dex") || lower.endsWith(".xml") || lower.startsWith("assets/")) {
                    val bytes = zip.getInputStream(entry).use { it.readBytes() }
                    val text = String(bytes, Charsets.ISO_8859_1)
                    RELEASE_FORBIDDEN_MARKERS.forEach { marker ->
                        if (text.contains(marker)) {
                            violations += "DEBUG: $marker in $name"
                        }
                    }
                }
            }
        }
        reportFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText(
                if (violations.isEmpty()) {
                    "OK: no model, replay media, debug HUD, or parity hooks in ${apk.name}"
                } else {
                    violations.joinToString("\n")
                },
            )
        }
        if (violations.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("Release APK must omit the model, replay media, debug HUD, and parity hooks (C12 / AD-10). Found:")
                    violations.forEach { appendLine("  - $it") }
                },
            )
        }
    }
}
