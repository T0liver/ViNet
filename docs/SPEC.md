# Grape Leaf Disease Detection: Android App Specification

Status: draft v1 (planning complete, ready for implementation)

## Identity

| | |
|---|---|
| App name | ViNet |
| Package, namespace, and applicationId | `hu.toliver.vinet` |
| Mark | `docs/vinet.svg` (shape) and `docs/vinet_c.svg` (color). A grape leaf on the left, a net of nodes on the right. The UX spine owns the recolor. |

## 1. Purpose

ViNet is an Android app that runs a YOLO26 object detector on the live camera feed while a user walks a vineyard or drives a tractor past the canopy. It detects leaf diseases and abnormalities, turns the stream of per-frame detections into deduplicated **events** (class, confidence, GPS, one image), and lets the farmer review where problems were found after the work is done.

The core challenge is not running the model. It is turning frame-level detections into trustworthy, geolocated, deduplicated field data.

## 2. Scope

### In scope
- Real-time on-device detection (camera, inference, overlay)
- Tracking and event logging with GPS and one evidence image per event
- Session storage, review (map and list), export
- Density and severity warnings per area
- Model tiers (several model sizes) selected by device capability
- Desktop-testable development: stand-in models, video replay, GPX replay

### Out of scope
- Model training, augmentation, and accuracy work (separate trainer project)
- Cloud sync, web dashboard, accounts
- Segmentation-based severity (possible later)
- External RTK/GNSS support (possible later, keep `LocationSource` open for it)

## 3. Hard constraints

1. **Offline-first.** Everything needed in the field works with no connectivity. Network use is limited to optional, user-visible downloads (model packs) done at home or on Wi-Fi. No telemetry, no sync.
2. **No Google Play services dependency.** Must run on devices without GMS (the S10 test device has none). No Firebase, ML Kit, Play Core, Google Maps SDK, Play-services LiteRT runtime, or Fused Location as the only option. Rule: no Google Play dependencies in the app module.
3. **Sideloadable APK.** Installable without the Play Store.
4. **minSdk 26 (Android 8)**, targetSdk = current stable at implementation time.
5. **Precision over recall** in the model. The app must tolerate sparse detections (see tracker gap tolerance).
6. **No hard-coded classes.** Class list, colors, thresholds come from the model pack manifest.

## 4. Architecture

### Pipeline

```
Camera -> preprocess -> Detector -> Tracker -> EventFilter -> EventLogger
                                                    |              |
                                              DensityEngine   Room + image store
                                                    |
                                         Live alerts / Review UI / Export
```

`LocationSource` feeds the EventLogger and DensityEngine in parallel.

### Modules (Gradle)

| Module | Responsibility |
|---|---|
| `:app` | UI (Compose), navigation, DI wiring, foreground service |
| `:core-model` | Pure Kotlin data types: Detection, Track, Event, Session, ModelPack manifest |
| `:inference` | `Detector` interface plus implementations (runtime backends, FakeDetector) |
| `:camera` | CameraX/Camera2 capture, exposure and focus control, frame source abstraction (live and video replay) |
| `:tracking` | Tracker, event filter, best-frame selection, merge rules. Pure Kotlin, JVM unit-testable |
| `:location` | `LocationSource` interface, implementations, interpolation, smoothing |
| `:data` | Room database, repositories, image store |
| `:density` | Coverage-normalized density and severity engine. Pure Kotlin |
| `:export` | GeoJSON, CSV, KML/GPX writers |
| `:devtools` | Video replay, GPX replay, debug HUD, parity harness hooks |

Rule: `:core-model`, `:tracking`, `:density` have no Android dependencies, so they test on the JVM without an emulator.

## 5. Key interfaces (contracts)

These are the seams that let the app be built without the real model.

```kotlin
data class Detection(
    val classId: Int,
    val score: Float,
    val box: NormalizedBox,      // 0..1 relative to the source frame, after un-letterboxing
    val frameTimestampNs: Long
)

interface Detector : AutoCloseable {
    val modelInfo: ModelInfo     // id, version, input size, class labels
    fun detect(frame: Frame): List<Detection>
}

interface LocationSource {
    val fixes: Flow<GeoFix>      // lat, lon, accuracy, speed, bearing, timestamp
    fun start()
    fun stop()
}
// Implementations: PlatformLocationSource (android.location.LocationManager),
// FusedLocationSource (only if GMS present, optional), GpxReplayLocationSource.

interface FrameSource {
    val frames: Flow<Frame>
}
// Implementations: CameraFrameSource, VideoReplayFrameSource.
```

Inference runtime is also behind an interface (`InferenceBackend`): candidates are bundled LiteRT, NCNN, ONNX Runtime Mobile. Pick by benchmark. Only bundled (non-Play-services) variants are allowed.

### Model pack

A model pack is a model file plus `manifest.json`:

```json
{
  "packId": "grape-yolo26n-fp16",
  "modelVersion": "0.0.0-standin",
  "family": "yolo26",
  "tier": "N",
  "file": "model.tflite",
  "sha256": "<hex>",
  "backend": "litert",
  "inputSize": [640, 640],
  "inputFormat": { "colorOrder": "RGB", "normalize": "0-1", "letterbox": true, "padValue": 114 },
  "outputFormat": "yolo26-end2end",
  "classes": [
    { "id": 0, "name": "Downy mildew", "color": "#2E7D32", "minScore": 0.55 },
    { "id": 1, "name": "Powdery mildew", "color": "#F9A825", "minScore": 0.55 }
  ],
  "minTrackFrames": 3,
  "notes": ""
}
```

Rules:
- The app reads classes, colors, thresholds, and preprocessing from the manifest only.
- Verify the actual exported output tensor layout against the real model. Do not assume.
- Every Event stores `modelVersion` and `packId`.
- Packs can be imported from a file and, optionally, downloaded (section 13).

## 6. Stand-in models (until the trained model exists)

1. **Pretrained COCO YOLO26n (same family).** Exercises the real pipeline: export, runtime, latency, thermal behavior. The manifest maps a few COCO classes (e.g. apple, orange, potted plant) to fake disease names so the UI looks realistic.
2. **FakeDetector.** Scripted, deterministic detections (e.g. "class 2 cluster visible for 30 frames then gone") for unit-testing tracker, event logic, and density with known expected results.

## 7. Camera

- CameraX `ImageAnalysis` with keep-only-latest backpressure.
- Camera2 interop: cap exposure time (short shutter, higher ISO), lock focus at a fixed distance, avoid stabilization that crops or warps. All values configurable.
- Handle sensor rotation and aspect ratio carefully (ties directly to letterbox parity).
- Preview is optional. A low-power "tractor mode" dims or turns off the screen while processing continues in a foreground service.
- Typical geometry: camera faces sideways at the canopy wall, roughly 0.5 to 1.5 m away. Settings: mount side (left/right) and offset from the phone's GPS position to the canopy point.

## 8. Tracking and events

- Lightweight tracker (IoU/ByteTrack-style) assigns track IDs.
- **Tolerant to gaps:** the precision-first model yields sparse detections, so use a longer max-age before a track ends.
- **One track = one event = one image.**
- An event is created when a track passes `minTrackFrames` and the per-class `minScore`. Per-class thresholds are configurable.
- **Best-frame selection:** keep the best candidate frame in memory (compressed JPEG if many tracks are open), replace when a better one arrives, write to disk only when the track ends. Score = weighted combination of:
  - confidence (largest weight)
  - box area, capped
  - box fully inside frame
  - sharpness (variance of Laplacian on the crop)
  
  Weights live in config.
- **Merge rule:** same class within X meters and Y seconds merges into the existing event (no second image). Both values configurable.
- Bound the number of simultaneously open tracks and buffered frames.

## 9. Location

- Interpolate position to the **frame timestamp** rather than using the latest fix.
- Smooth the track (simple Kalman or moving filter) and discard or flag low-accuracy fixes.
- Store accuracy, speed, and bearing on every event.
- Apply the configured camera mount offset (side and distance) to produce the canopy position.
- Optional plot polygons: import GeoJSON/KML, tag each event with the plot name via point-in-polygon.
- Foreground service with wake lock so sessions survive screen-off.
- Phone GPS accuracy (about 3 to 5 m) is accepted. The stored image is the farmer's way to identify the exact spot.

## 10. Data model (Room)

**Session:** id, start, end, deviceInfo, packId, modelVersion, settings snapshot, notes, plot name.

**Event:** id, sessionId, timestamp, lat, lon, accuracy, speed, bearing, classId, className, confidence, trackId, bbox, imagePath, thumbPath, plotName, userFeedback (none/correct/wrong/unsure), modelVersion.

Each event has exactly one image path. No separate images table.

**Image storage:**
- Full frame (evidence), JPEG quality about 80, long side capped (e.g. 1280), plus small crop thumbnail.
- EXIF GPS written to the full frame.
- Per-session and global size caps with a visible storage meter.
- Setting: keep images only above a confidence level.
- Cleanup option: delete images of old sessions but keep event rows.

## 11. Density and severity

Goal: a stationary phone must not read as high disease density.

- Count **unique events only**, never frames or raw detections.
- Divide the path into segments (cells of 5 to 10 m, or per N meters traveled). Compute **events per meter traveled** (or per minute of active scanning).
- When speed is below a threshold, accumulate no new coverage. Repeated detections in the same cell merge.
- Severity proxy without segmentation: fraction of the frame covered by detection boxes, averaged per segment.
- Live alert: if density over the last N meters crosses a configurable threshold, show "high disease pressure here" and flag that segment on the review map.
- Aggregate spatially: clusters of events matter more than any single detection.

All thresholds are config, tuned in real-phone testing.

## 12. Device tiers, benchmark, thermal

- Ship or offer model tiers (e.g. N / S / M).
- **First-run benchmark** picks the largest tier that sustains a target FPS (about 8 to 10) without throttling. Users can override in settings. If even the smallest tier cannot sustain the minimum FPS, say so clearly.
- Benchmark input: see open question 3 (decision pending). Whatever the input, run it long enough to expose throttling.
- **Thermal adaptation:** listen to the thermal status API (API 29+). When the device heats up, lower the inference rate or suggest a lighter tier. Degrade gracefully instead of overheating.
- **Advisory** on first run: bring a power bank, keep the phone out of direct sun, start with a cool phone, use a ventilated mount.
- All benchmark and thermal parameters (target FPS, temperature thresholds, rate steps) are **config values**, so real-phone tuning changes numbers, not code.

## 13. Model pack distribution

- Models are not bundled in the APK beyond a minimal stand-in or the smallest tier.
- Optional download on first run (Wi-Fi, user-visible, with progress and retry), from a hosted index (`index.json` listing packs, versions, URLs, SHA-256, sizes).
- **Always verify SHA-256** before using a downloaded pack.
- Packs can also be imported from local storage (no network needed).
- Hosting options: Hugging Face (good model versioning and direct file URLs), GitHub Releases, or a self-hosted file server. Kaggle is less convenient for unauthenticated direct downloads.
- The app must work fully with a locally imported pack and never block field use on connectivity.

## 14. Review and export

- Session map (MapLibre, offline tiles, no Google Maps) with markers or heatmap by class and flagged high-density segments.
- Filterable list with thumbnails and tap-to-view full frame.
- Feedback per event: correct / wrong / unsure.
- "Navigate back to this spot" action (open location in any installed maps/navigation app, or show bearing and distance in-app).
- Export: GeoJSON, CSV, KML/GPX, plus a zip including images. Share via the Android share sheet so users can send to Drive, a file server, etc. This is export only, not sync.
- Optionally export flagged or low-confidence frames as a retraining set.

## 15. Dev and test tooling

- **Video replay** through the exact production pipeline (reproducible model comparisons on identical footage).
- **GPX replay** as a `LocationSource`.
- **Debug HUD:** FPS, inference latency, temperature, open tracks.
- **Parity harness:** run a fixed set of 100 to 200 images through Python (reference) and through the app, compare detections. Catches letterbox, RGB/BGR, normalization, and rotation mismatches. Build it before the real model arrives.
- **Unit tests** (JVM) for tracker, event filter, best-frame scoring, merge rules, density engine, polygon tagging, exporters.
- Long-run (30+ min) thermal test is part of real-phone testing.

## 16. Stack

Kotlin, Jetpack Compose, CameraX (+ Camera2 interop), Room, Hilt, WorkManager (exports, downloads), `android.location.LocationManager`, MapLibre, bundled inference runtime behind `InferenceBackend`. Pin dependency versions. Check each dependency for hidden Play services requirements.

## 17. Milestones

Milestones 1 to 7 need no phones beyond one dev device or emulator, and no trained model.

| # | Milestone | Done when |
|---|---|---|
| 1 | **Foundations** | Modules, `Detector`/`LocationSource`/`FrameSource` interfaces, manifest parser, Room schema, CI running JVM tests |
| 2 | **Inference spike** | COCO YOLO26n runs on a still image through the real backend; parity harness exists and passes on stand-in |
| 3 | **Live camera** | Real-time overlay, debug HUD, exposure/focus control, video replay as frame source |
| 4 | **Tracking and events** | Tracker, gap tolerance, event filter, best-frame, merge rules, all unit-tested with FakeDetector and replay |
| 5 | **Sessions and location** | Foreground service, platform location source, interpolation, smoothing, GPX replay, mount offset, plot polygons, image storage with caps |
| 6 | **Review and export** | Offline map, list, feedback marking, GeoJSON/CSV/KML export, density and severity layer, live alert, navigate-to-spot |
| 7 | **Device and safety layer** | Benchmark and tier selection, thermal adaptation, advisory, settings, model pack import/download with checksum |
| 8 | **Real-phone testing and tuning** | S23+ (high-end), S10 (no GMS, Android 14), a low-end family phone. Handheld, then tractor-mounted. Tune FPS targets, tiers, thermal, battery, density thresholds |
| 9 | **Model swap** (blocked on training) | Real model pack imported, parity test passes, per-class thresholds tuned, quantization and input resolution decided, final field test with ground truth |

## 18. Risks

| Risk | Handling |
|---|---|
| Domain shift, false positives, motion blur | Out of app scope (training). App records model version and keeps evidence frames for later retraining |
| Thermal and battery | Advisory, thermal API adaptation, tier selection, tractor mode |
| GPS precision | Accepted. Mitigated by images, plot polygons, smoothing, notes |
| Runtime or output-format mismatch with real model | Parity harness, manifest-driven preprocessing, verify real export early |
| Hidden Play services dependency | "No GMS" rule, periodic run on the S10 |
| Storage growth | One image per event, caps, cleanup |

## 19. Decisions log

- Devices: broad range, benchmark-gated, multiple model tiers.
- Classes: about 6 to 8, defined only in the model pack manifest.
- Data: fully on-device, export only, no sync.
- Severity: coverage-normalized density plus frame-coverage proxy, no segmentation for now.
- Model priority: precision over recall.
- minSdk 26. S10 runs Android 14.
- No Play services dependency anywhere in the app module.
- One image per event, best frame by combined score.
- Real-phone testing happens late (milestone 8).
- App name: ViNet. Package: `hu.toliver.vinet`.
- Brand: vine green and sky blue, with the grape-leaf mark. Class colors stay in the model pack.

## 20. Open questions

1. Model hosting: Hugging Face vs GitHub Releases vs self-hosted.
2. Which inference backend wins (decided by benchmark after the spike).
3. **Benchmark input.** Proposal: latency and thermal behavior do not depend much on image content for an end-to-end (NMS-free) model, so loop one or a few small bundled still images instead of downloading a video. This avoids a network dependency at first launch and keeps the benchmark fully offline. A downloadable benchmark clip would only be needed for accuracy checks, which belong in the parity harness, not the first-run benchmark. Decision pending.
4. Whether model tiers beyond N are downloaded or user-imported.
5. Exact density thresholds and segment sizes (tuned in milestone 8).
