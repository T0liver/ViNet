---
type: initiative
title: Grape leaf disease detection
parent: none
sources:
  - docs/SPEC.md
companions:
  - docs/SPEC.md
  - _bmad-output/initiative-grape-leaf/architecture-grape-leaf/architecture-grape-leaf.md
  - _bmad-output/initiative-grape-leaf/ux-grape-leaf/DESIGN.md
  - _bmad-output/initiative-grape-leaf/ux-grape-leaf/EXPERIENCE.md
risk: high
---

# Grape leaf disease detection

## Description

ViNet is an Android app a farmer can sideload and use in a vineyard with no network. It watches the canopy from the hand or from a mount, turns the live detections into one geolocated event per sighting, and lets the farmer review and export that record afterward. The product contract is `docs/SPEC.md`. The architecture spine and the experience and design spines are the shared decisions.

## Outcome

A farmer finishes a canopy scan on a phone with no Play services and, still offline, can point to where the problems were, with one photo per sighting.

## Requirements

- R1 (SPEC §1, §2, §7): A farmer can scan a canopy on foot or from a mount and see the latest boxes labeled with the pack class and score, and the scan keeps running when the preview is hidden or the screen is off. Met when a running session shows those boxes and still runs after the preview is hidden or the screen is off.
- R2 (SPEC §2, §8, §9, §10): Each new sighting becomes one event with class, confidence, and at most one full-frame photo and one thumbnail. A fix inside `maxAccuracyM` is stored as the canopy point, with accuracy, speed, and bearing. A stored fix worse than `weakAccuracyM`, seeded at 5 m, is labeled "Weak GPS". When every fix is outside the cap, the event is still stored, with its photo and no canopy point, and the screen says "location is missing". The same class within the configured distance and time stays that event and keeps its photo. Met when a known replay writes one row and one image, a nearby repeat does not write a second row, and a replay with no fix inside the cap writes the row with no canopy point.
- R3 (SPEC §2, §14): The farmer can open a finished session on an offline map and in a list, filter by class, plot, and feedback, mark an event correct, wrong, or unsure, and open its canopy point in another maps app or read bearing and distance. Met when this works with no network, and with no tiles the list still works.
- R4 (SPEC §2, §14): The farmer can export that session as GeoJSON, CSV, KML/GPX, a zip of images, and a retraining file, and hand the file off through the Android share sheet. The retraining file contains events marked wrong or unsure, plus events under the configured confidence. The session stays on the phone. Met when each format is produced and the share sheet opens.
- R5 (SPEC §2, §11): While the farmer is moving, the app shows the sentence "high disease pressure here" when events per meter cross the configured threshold, and the review map flags that segment with density and severity as separate values. With the screen off, that sentence is the service notification, and the notification vibrates once. A scan that is not moving does not raise the alert. Met when a moving replay crosses the threshold and a stationary one does not.
- R6 (SPEC §2, §6, §12, §13): A release APK contains no model file. The farmer imports a pack from local storage with no network, or downloads one from a configured index on Wi-Fi, and a download is installed only after the checksum matches. The app selects the largest installed tier that holds the configured frame rate, and the farmer can override that choice for the next session. If the smallest tier cannot, the app says so and still opens a scan. A debug APK may bundle a stand-in for the first test. Met when a release install cannot scan until a pack is imported or downloaded, a bad checksum leaves the previous pack in place, and a local import is enough.
- R7 (SPEC §2, §6, §15): On a debug build, a developer can point the same scan path at a video and a GPX file, use a stand-in or scripted detector, and read the debug figures. A fixed image set run in the app matches a reference run closely enough to catch preprocess mismatches. The release build does not include the stand-in model, the replay media, the debug HUD, or the parity hooks. Met when a debug replay yields the same kind of overlay and events as a camera session and the stand-in parity check passes.
- R8 (SPEC §17 milestone 9): A trained grape-leaf pack can replace the stand-in, pass that same parity check, and be accepted by a field comparison against ground truth. Met when that pack is the active pack on a real row and parity passes. It waits on the trainer project.

## Constraints

- C1 (SPEC §3.1, AD-9): Capture, review, and export work with no network. The only network use is an optional, user-visible pack download on unmetered Wi-Fi. There is no telemetry, account, or sync.
- C2 (SPEC §3.2, §3.3, AD-9): The app has no Play services dependency, including transitively, and the APK is sideloaded. Fused location is not a dependency of the app.
- C3 (SPEC §3.4, architecture stack): minSdk is 26 and targetSdk is 37.
- C4 (SPEC §3.5, AD-4): The model favors precision, so a track ends only when its configured max age is exceeded, not because a frame had no detections.
- C5 (SPEC §3.6, AD-3, DESIGN): Classes, colors, score thresholds, and preprocessing come only from the model-pack manifest. Class color is not a theme token.
- C6 (AD-1): The foreground service is the only capture pipeline. Replay and the scripted detector are port implementations. The UI does not detect, own tracks, or write events.
- C7 (AD-2): core-model, tracking, density, and export import no Android APIs. Inference, camera, location, and data depend on core-model and not on each other.
- C8 (AD-5, AD-6): Tracking decides create, merge, and the best frame. Data is the only writer. A merge keeps the existing image. Export is a pure function of rows the app has loaded.
- C9 (AD-7, AD-13): The stored position is the canopy point after the mount offset. The frame and the fix are joined on elapsed realtime, never on the latest fix or on UTC. A fix worse than `maxAccuracyM` is excluded from the join and from distance. `weakAccuracyM`, seeded at 5 m, is only the "Weak GPS" label.
- C10 (AD-8): Density counts unique events per meter traveled while speed is at or above the configured minimum. Severity is the separate per-segment box-area fraction.
- C11 (AD-11): The app passes one immutable config snapshot into the other modules, and the session stores that snapshot. The key names are fixed, including `weakAccuracyM`. The numbers are tuned on phones.
- C12 (AD-10): Inference goes through InferenceBackend, and only bundled runtimes are allowed. The benchmark loops still images that ship on the device and does not download media. A release APK contains no model file. A debug APK may bundle a stand-in model and a video for the first test. Thermal adaptation runs on API 29 and above and does nothing below.
- C13 (AD-12): Data mints one opaque id per session and per event. Stored instants are UTC epoch milliseconds. Exports render those instants as ISO-8601 UTC.
- C14 (SPEC §1, §19, DESIGN): The app is ViNet, package `hu.toliver.vinet`. The mark is the grape-leaf drawing, chrome is vine and sky, and the wordmark is Fraunces.

## Done when

- A sideloaded APK with no Play services finishes an offline scan, and the events show on the map and in the list.
- A repeated sighting of the same class stays one event and one photo.
- The pressure sentence appears only while the phone is moving and the threshold is crossed.
- The release APK contains no model file. A pack imported from local storage is enough to scan, and a checksum mismatch is refused.
- A high-end phone, an S10 with no Play services, and a low-end phone have each run a handheld scan and a mounted scan.
- Until a trained pack exists, the stand-in path is the acceptance of the checks above.

## Boundaries

Training, augmentation, and accuracy work stay in the trainer project. Cloud sync, accounts, and a web dashboard are out. Segmentation-based severity and external RTK are out. Play Store distribution, tablets, the web, and watches are out.

Tracer path: on a debug build, with the bundled stand-in, run one replay through the foreground service, see one labeled box, stop, and open one event that has a photo and a canopy point. The release build reaches that same screen only after a pack is imported or downloaded.

- Touch point: trainer project — a future grape-leaf pack only; owner: epic-model-swap
- Touch point: pack index host — a configured URL and the index fields; GitHub is allowed and not required; owner: epic-packs
- Touch point: map tiles already on the device — review reads them and never downloads them; owner: epic-review
- Touch point: another maps app — a geo intent for the canopy point; owner: epic-review
- Touch point: Android share sheet — the only way a file leaves the phone; owner: epic-review

## References

- spec — docs/SPEC.md, sections 1–20
- architecture — _bmad-output/initiative-grape-leaf/architecture-grape-leaf/architecture-grape-leaf.md, AD-1 through AD-13
- design — _bmad-output/initiative-grape-leaf/ux-grape-leaf/DESIGN.md
- experience — _bmad-output/initiative-grape-leaf/ux-grape-leaf/EXPERIENCE.md

## Notes

- Decision: 2026-11-08 Bottom navigation is Scan, Sessions, and Settings. Packs and Benchmark stay steps inside that, not tabs.
- Decision: 2026-11-08 The experience spine's voice-table sentences are the product copy.
- Decision: 2026-11-08 The retraining export is in Review and export. It contains events marked wrong or unsure, plus events under the configured confidence.
- Decision: 2026-11-08 A Settings row imports offline map tiles from local storage. Review reads those tiles and never downloads them.
- Decision: 2026-11-08 The pressure notification vibrates once when it appears, including when the screen is off.
- Decision: 2026-11-08 In landscape, session review places the map and the list side by side. Scan keeps its stacked controls in both orientations.
- Decision: 2026-11-08 A release build omits the debug HUD and the parity hooks. Both stay on the debug build.
- Decision: 2026-11-08 Benchmark loops still images that ship on the device, long enough to show throttling. A video is not the benchmark input.
- Decision: 2026-11-08 A release APK contains no model file. A pack is downloaded from a configured index or chosen from local storage. A debug APK may bundle a stand-in and images or a video for the first test.
- Decision: 2026-11-08 Plot polygons are imported once in Settings and reused. The start sheet keeps the session name and notes only.
- Decision: 2026-11-08 The pack host stays out of the backlog. GitHub is an allowed host and is not required.
- Decision: 2026-11-08 The post-spike benchmark chooses the default runtime. The spike starts on LiteRT. That start is not the field default.
- Decision: 2026-11-08 A fix outside `maxAccuracyM` leaves the event with no canopy point, and the screen says "location is missing". A fix inside the cap that is worse than `weakAccuracyM` (seeded at 5 m) is labeled "Weak GPS". The event is never discarded for GPS.
- Decision: 2026-11-08 Real model swap stays last and unstarted. Epics 1 through 7 are accepted on a stand-in pack.
