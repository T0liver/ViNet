# Spec reconcile — grape-leaf architecture spine

- **Date:** 2026-11-08
- **Spec:** `docs/SPEC.md` (draft v1)
- **Spine:** `_bmad-output/initiative-grape-leaf/architecture-grape-leaf/architecture-grape-leaf.md`
- **Spine edited:** no
- **Verdict:** fail

Fail means a requirement, constraint, or tone in the spec is missing or contradicted in a way that lets two epics obey the spine and still ship incompatible behavior. Detail the spine may defer, and spec choices the spine narrows without reversing, are not findings.

## Findings

### 1. Density never sees the path or the frame boxes — high — autofix

Spec §4 draws `LocationSource` into both the event logger and the density engine. §11 then defines density as unique events per meter traveled, with no new coverage while speed is under a threshold, and severity as the fraction of each frame covered by detection boxes, averaged over the segment. The live alert is density over the last N meters.

The spine diagram joins `LocationSource` only into `EventFilter`, and joins `DensityEngine` only to filter output. AD-8 states the right formulas and forbids the UI from recomputing them from frames, but it never names the inputs. A pipeline epic can pass events only. A density epic can treat AD-8 as event-to-event distance, the speed stored on those events, and the one box kept for each event. A location epic can keep the continuous fix stream for the canopy join in AD-7 and AD-13. Meters traveled, the slow-speed gate, and severity then disagree, which is the failure the spec's opening states: a stationary phone must not read as high disease pressure.

AD-8's "repeated events in a segment merge" must stay a counting rule. The only event-identity merge is AD-6 (same class, configured distance and time).

### 2. The evidence file is unspecified — high — autofix

Spec §8 and §10: one evidence image per event, and that image is the full frame (JPEG, long side capped), plus a small crop thumbnail. EXIF GPS is on the full frame. §14's review opens the thumbnail and then the full frame.

AD-5 and AD-6 require one in-memory best frame, one `imagePath`, one `thumbPath`, and an EXIF point that matches the canopy coordinate. They do not say the evidence file is the full frame and the thumb is the crop. Tracking can write the sharp crop as `imagePath` and a scaled copy as `thumbPath`. Review can treat `imagePath` as the full frame. Both obey the spine.

JPEG quality, the long-side cap, and the storage meter are tuning and one screen. They are not this finding.

### 3. Manifest preprocessing is missing `padValue` and `minTrackFrames` — high — autofix

Spec §3.6 and §5: classes, colors, thresholds, and preprocessing come from the pack manifest only. The manifest includes `inputFormat.padValue` and `minTrackFrames`. §5 also says not to assume the export layout; AD-3 already binds that for `outputFormat`.

AD-3's manifest list is input size, color order, normalization, letterbox, and per-class minimum score. It omits pad value and `minTrackFrames`. AD-11 says new thresholds are fields on the config snapshot, not new stores. The pack epic can leave pad and `minTrackFrames` out of `ModelPack`. Preprocess can hard-code pad 114. Tracking can read `minTrackFrames` from the config object. Letterbox parity and the event gate then depend on which epic a pack was built for.

Per-class minimum score is already bound to the manifest. That part matches §3.6. "Configurable" in §8 is the manifest, not a second user store.

### 4. Rotation and warp have no owner — high — autofix

Spec §7: sensor rotation and aspect ratio stay consistent with letterbox parity, and stabilization must not crop or warp. §15's parity harness exists to catch rotation mismatches. §2's geometry is a canopy wall about 0.5 to 1.5 m to the side.

AD-3 gives `:inference` the one preprocess and defines `NormalizedBox` on the source frame after letterbox removal. It does not say who applies sensor rotation, and it does not forbid a capture mode that crops or warps before that frame exists. Camera can rotate in CameraX while preprocess rotates again, or neither can. Camera can enable a stabilizing crop while parity still images stay unwarped. Overlay, stored boxes, and the harness can all use 0..1 of whatever bytes they received and still disagree with each other.

### 5. Canopy offset, bad fixes, and plot polygons can each be invented twice — medium — autofix the offset; defer the open choice and the file format

Three location contracts are shared and unset:

- Spec §7: the offset is mount side (left or right) plus distance from the phone fix to the canopy. AD-7 says only "configured mount offset." Settings can store a forward scalar. Location can apply a lateral offset using bearing. The stored canopy point then differs.
- Spec §9: low-accuracy fixes are discarded or flagged, and accuracy is stored on every kept event. AD-7 requires the stored accuracy and does not pick discard versus flag, and Deferred does not name the choice. Location can drop the fix. Review can expect a flag on the event row.
- Spec §9: plot polygons are imported GeoJSON or KML, and `event.plotName` is point-in-polygon. AD-7 assigns the pure function to `:location` and keeps `session.plotName` as a label. AD-5's only writers are sessions, events, and image files. Import can persist polygons in `:data` as GeoJSON while location reads a KML file, or the reverse. Tagging then sees a different set than import.

`session.plotName` not copied onto events is the right reading of §9 and §10. Leave that rule.

### 6. Model download is not held to Wi-Fi — medium — autofix

Spec §3.1 and §13: the only network use is an optional, user-visible model-pack download, done at home or on Wi-Fi, with progress and retry. Field use never waits on the network.

AD-9 binds optional, user-visible, checksummed download, and WorkManager outside the capture service. It does not bind unmetered or Wi-Fi. The download epic can run on cellular. Nothing else in the spine conflicts with that, so the hard constraint is simply gone.

Progress and retry are one worker's UI. The index fields are already deferred.

## Not findings

These are bound, or the spine may defer them, or the spine narrows the spec without reversing it.

- Offline capture, review, and export; no telemetry, accounts, or sync; sideloadable APK; `minSdk` 26; no Play services on `:app`, including transitively; fused location never the only source and not a dependency of `:app`.
- One foreground-service pipeline. Replay and `FakeDetector` implement the ports. `:devtools` does not own a second pipe. UI does not own tracks or write events.
- Module direction, JVM purity of `:tracking`, `:density`, and `:export`, and Hilt only in Android modules. Export staying Android-free is stricter than the module table and matches the JVM exporter tests.
- Synchronous `Detector.detect`, Flow ports, keep-only-latest, gaps not filled with invented boxes.
- One writer in `:data`. Best-frame bytes written once when the track ends. Merge keeps the existing image. `trackId` stays the creating track. `packId` and `modelVersion` on every event. No images table. Missing file when confidence skips it or cleanup deletes bytes and keeps the row.
- Canopy `lat`/`lon`, matching EXIF, accuracy, speed, bearing, meters and meters per second. Frame/fix join on elapsed realtime, wall clock separate. Spec named one timestamp and "not the latest fix"; AD-12 and AD-13 close that clock fork.
- Classes, colors, and per-class minimum score only from the manifest. Decoder selected by `outputFormat`. One preprocess for live, still, and parity. Normalized box space shared with overlay and the harness.
- Density counts events per meter, not frames, and does not use box area as the count. Severity is a separate box-fraction. The spec also allowed per minute of active scanning; per meter is a legal narrowing. Threshold numbers stay deferred.
- Tiers, on-device benchmark input, user override, smallest-tier failure spoken aloud, thermal adaptation only on API 29+. Backend winner, index host, which tiers are listed, and benchmark media type stay deferred. "Both import and download exist" closes open question 4 without dropping either path.
- MapLibre on tiles already on the device. Navigate-to-spot as a geo intent. The spec also allowed in-app bearing and distance; the geo intent is a legal narrowing. Share sheet is export, not sync. GeoJSON, CSV, KML/GPX, and a zip of images. Opaque ids. UTC epoch milliseconds rendered as ISO-8601 UTC. WGS84.
- One config snapshot on the session. Pure modules do not read their own preferences.
- Tracker algorithm, best-frame factor formula, numeric weights, segment length, JPEG quality, size caps, advisory copy, debug HUD fields, COCO stand-in class names, the "about 6 to 8 classes" note, the S10/S23 test matrix, and the optional retraining-set export. One owner, or tuning, or an optional later path.
- Stack pins. `targetSdk` = current stable is satisfied by the pinned API 37 seed; this review does not re-check versions.

## Disposition

Autofix findings 1–4 and 6 in the spine. For finding 5, state the offset as side plus distance, and either pick or explicitly defer low-accuracy handling and the polygon import format. Name `:data` or `:location` as the single polygon store either way.
