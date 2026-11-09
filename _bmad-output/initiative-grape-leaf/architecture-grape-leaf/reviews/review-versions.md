# Version review — grape-leaf architecture spine

- **Date checked:** 2026-11-08
- **Subject:** `_bmad-output/initiative-grape-leaf/architecture-grape-leaf/architecture-grape-leaf.md` (Stack table, and versions or technology names in the architecture decisions)
- **Spine edited:** no
- **Verdict:** pass

Pass means every committed version number was a real published stable, or a real platform API, on 2026-11-08, and none of those pins requires Play services or a `minSdk` above 26. It does not mean every pin is the newest major line, or that the set builds with no extra toolchain pin.

## Scope

Checked the Stack table (Kotlin 2.4.21, Android Gradle Plugin 9.5.0, compileSdk/targetSdk 37, minSdk 26, Compose BOM 2026.09.00, CameraX 1.6.2, Room 2.8.5, WorkManager 2.12.0, Hilt 2.60.1, kotlinx-coroutines 1.11.0, MapLibre Android SDK 13.7.1).

Architecture decisions name `minSdk` 26 (AD-9), API 29 thermal behavior (AD-10), and unversioned candidates: bundled LiteRT, NCNN, and ONNX Runtime Mobile. No other numeric versions appear in the decisions.

Constraint used for fit: offline Android app, `minSdk` 26, no Play services on the `:app` classpath (AD-9).

## Result by pin

| Name | Spine | Real on 2026-11-08 | Latest stable of that artifact | minSdk vs 26 | Play services |
| --- | --- | --- | --- | --- | --- |
| Kotlin | 2.4.21 | Yes. Released 2026-10-08. Current stable. 2.5.0-Beta2 is preview only. | Yes | n/a | No |
| Android Gradle Plugin | 9.5.0 | Yes. Stable, notes last updated 2026-10-28. Maximum API is 37. | Yes. No newer stable found. | n/a | No |
| compileSdk / targetSdk | 37 (Android 17) | Yes. Android 17 / API 37 shipped 2026-06-16. | Yes, as the current platform | App minSdk 26 is below it | No |
| minSdk | 26 | Yes. Android 8.0. | n/a (product floor) | Is the floor | No |
| Compose BOM | 2026.09.00 | Yes. Published 2026-09-09. Android docs still name it the latest stable BOM (pages updated 2026-10-28). Maps core Compose to 1.12.1. | Yes. Compose 1.13.0-rc01 (2026-11-04) is not stable and is not in this BOM. | BOM libraries support API 21+; Compose 1.12 wants compileSdk 37, which the spine already sets | No |
| CameraX | 1.6.2 | Yes. Released 2026-08-26. | Yes. 1.7.0-rc02 exists and is not stable. | Library floor is API 21 | No |
| Room | 2.8.5 | Yes. `androidx.room` 2.8.5 released 2026-09-09. | Latest **2.x** only. See finding 1. | Floor is API 23 | No |
| WorkManager | 2.12.0 | Yes. `androidx.work` 2.12.0 released 2026-09-23. | Yes | Raised its own floor from 23 to **24**, still under 26 | Not on `work-runtime`. See finding 4. |
| Hilt | 2.60.1 | Yes. `com.google.dagger` 2.60.1 published 2026-07-06. Still the newest Dagger/Hilt release. | Yes, as a version number | Floor became 23 in 2.60 | No |
| kotlinx-coroutines | 1.11.0 | Yes. Released 2026-05-08. Built with Kotlin 2.2.20. | Yes. 1.11.1 is an empty milestone, not a release. | JVM/Android library; no Play services and no floor above 26 | No |
| MapLibre Android SDK | 13.7.1 | Yes. Tag `android-v13.7.1` on 2026-10-15; Maven date 2026-10-16. Stable, not a globe build. | Yes, on the stable line. See finding 3. | Floor has been 23 since 12.0.0 | No, on the published POM |

## Findings

### 1. Room 2.8.5 is real, and it is the maintenance line

`androidx.room:room-*:2.8.5` is the current Room 2.x stable. Google’s Room 3 migration guide treats 2.x as maintenance once Room 3 is stable. Room 3 is stable: `androidx.room3:room3-*:3.0.3` (2026-09-09), with the Room 3 page last showing a 2026-10-21 update and a 3.1.0 alpha. Room 3 is a different package (`androidx.room3`), Kotlin/KSP/coroutines only, and its floor is also API 23.

For this greenfield Kotlin app, 2.8.5 is a valid offline pin and not an invented one. It is the pin that will age first. It does not pull Play services.

### 2. Hilt 2.60.1 does not yet ship Kotlin 2.4 metadata support

Hilt 2.60.1 is the latest Hilt release. Its own notes move the project Kotlin version to 2.3.21. Using it on Kotlin 2.4.x still hits the known processor error: Hilt’s shaded metadata reader rejects metadata version 2.4.0 because its maximum is 2.3.0 (Dagger issues 5177 and 5180). The published workaround is to put `org.jetbrains.kotlin:kotlin-metadata-jvm:2.4.21` on the KSP classpath. That extra pin is not in the stack table.

KSP is also unpinned. The KSP line that Kotlin’s own 2.4.21 quickstart uses is 2.3.x, not a 2.4.x artifact. Current KSP stable is 2.3.13 (2026-10-30). KSP 2.3.10 and later include the Kotlin 2.4.0 module-name fix. Dagger’s KSP guide says Dagger/Hilt KSP is stable from Dagger 2.60 with KSP 2.3.9+.

This is a build-combination gap, not a fake Hilt version and not a Play-services dependency.

### 3. MapLibre 13.7.1 is the stable SDK; a newer globe pre-release shares the coordinates

`org.maplibre.gl:android-sdk:13.7.1` is the stable Android SDK. GitHub marks `android-v2026.11.2-globe` (2026-11-02) as a pre-release. Maven listings can still sort `2026.11.2-globe` as a newer version of the same artifact. A floating or “latest” resolution would pick the globe build. The spine’s explicit 13.7.1 pin is the correct stable choice.

The 13.7.1 POM depends on GeoJSON, gestures, Turf, annotations, Fragment, OkHttp, Timber, and coroutines. It does not depend on Play services or Google Maps. OkHttp is only a problem if review starts fetching tiles; AD-9 already forbids that. `minSdk` 23 is under 26. Do not substitute a `*-with-plugin-api` artifact unless a custom layer type is actually required; 13.7.1’s plugin API is experimental and lives on those separate artifacts.

### 4. WorkManager 2.12.0 fits; `work-gcm` would not

`androidx.work:work-runtime:2.12.0` depends on AndroidX, Kotlin, coroutines, and Room 2.7.0. It does not declare `play-services-basement`. Its `minSdk` of 24 is under 26. It is a good fit for sideloaded offline background download and export.

The release notes still list `androidx.work:work-gcm` as an optional GCMNetworkManager artifact. That artifact is the Play-services path and must stay off the `:app` classpath. `work-analytics` is experimental and is not part of this pin.

### 5. AGP 9.5.0 is current and matches API 37; Gradle 9.6.0 is not in the table

AGP 9.5.0’s compatibility table requires Gradle 9.6.0 (minimum and default), JDK 17, and build-tools 36.0.0, and its maximum API is 37. compileSdk/targetSdk 37 is consistent with that. The stack table does not pin the Gradle wrapper. That is a missing companion pin, not a wrong AGP version.

Android 17 behavior for apps that target API 37 (large-screen resizability, safer native dynamic code loading) is a product constraint of the pin, not evidence the pin is invented.

### 6. Unversioned AD-10 runtimes: bundled LiteRT is the fit; Play-services LiteRT is not

AD-10 names bundled LiteRT, NCNN, and ONNX Runtime Mobile with no versions, so there is no version to confirm. All three exist as on-device runtimes and can ship in a sideloaded APK.

LiteRT also exists as a Play-services distribution (`play-services-tflite-java` / `play-services-tflite-gpu`, called out in current Play services release notes). AD-9 already bans Play-services LiteRT, ML Kit, Firebase, Play Core, the Google Maps SDK, and fused location. The safe reading of “bundled LiteRT” is the on-device artifact, not the Play-services one. NCNN and ONNX Runtime Mobile do not pull Play services by themselves.

## What was not out of date

- Kotlin 2.4.21 is the current stable. 2.5.0-Beta2 (2026-10-21) is not a reason to move the pin.
- Compose BOM 2026.09.00 is still the stable BOM Google’s setup docs name. The 2026-11-04 Compose library pages show 1.12.1 stable, which this BOM already maps, and 1.13.0-rc01 as a release candidate.
- CameraX 1.6.2 is the current stable. 1.7.0-rc02 is not.
- kotlinx-coroutines 1.11.0 is the current stable even though it was built with Kotlin 2.2.20. No 1.11.1 or 1.12.0 has shipped. Using it from Kotlin 2.4.21 is normal for this library.
- Hilt 2.60.1 is the current Hilt release (four months old, and nothing newer exists). The gap is Kotlin 2.4 metadata, not a missed 2.61.

## Sources

- Kotlin 2.4.21: https://kotlinlang.org/docs/releases.html and https://github.com/jetbrains/kotlin/releases/tag/v2.4.21
- AGP 9.5.0 and the API 37 maximum: https://developer.android.com/build/releases/agp-9-5-0-release-notes and https://developer.android.com/build/releases/about-agp
- Android 17 / API 37: https://android-developers.googleblog.com/2026/06/Android-17.html and https://developer.android.com/about/versions/17/setup-sdk
- Compose BOM 2026.09.00: https://developer.android.com/develop/ui/compose/bom and https://mvnrepository.com/artifact/androidx.compose/compose-bom/2026.09.00
- CameraX 1.6.2: https://developer.android.com/jetpack/androidx/releases/camera
- Room 2.8.5 and Room 3.0.3: https://developer.android.com/jetpack/androidx/releases/room and https://developer.android.com/jetpack/androidx/releases/room3 and https://developer.android.com/training/data-storage/room/migration-2-to-3
- WorkManager 2.12.0 and its dependencies: https://developer.android.com/jetpack/androidx/releases/work and https://mvnrepository.com/artifact/androidx.work/work-runtime/2.12.0/dependencies
- Hilt 2.60.1: https://github.com/google/dagger/releases/tag/dagger-2.60.1 and https://github.com/google/dagger/issues/5177 and https://dagger.dev/dev-guide/ksp.html
- KSP 2.3.13 with Kotlin 2.4.21: https://github.com/google/ksp/releases and https://kotlinlang.org/docs/ksp-quickstart.html
- Coroutines 1.11.0: https://github.com/Kotlin/kotlinx.coroutines/releases/tag/1.11.0
- MapLibre 13.7.1 versus `2026.11.2-globe`: https://github.com/maplibre/maplibre-native/releases and https://mvnrepository.com/artifact/org.maplibre.gl/android-sdk/13.7.1
