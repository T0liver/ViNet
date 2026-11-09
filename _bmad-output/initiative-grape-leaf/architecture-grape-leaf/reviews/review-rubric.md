# Architecture Spine Rubric — Grape leaf detection

## Overall verdict

Fail. The spine is a real consistency contract: one pipeline, one module direction, one box space, one writer, one join clock, and an explicit offline envelope. It still fails the good-spine bar. Two rules do not prevent the divergence they name (AD-11, AD-3), two adopted rules contradict each other (AD-5 and AD-2), and one Deferred item leaves a choice two epics can make differently.

## Fixes that matter

1. Rewrite AD-11 so only `:app` reads preferences or other settings storage. Every other module receives the immutable snapshot.
2. Name the source-frame orientation in AD-3. `NormalizedBox` is 0..1 of the upright frame after rotation is applied and letterbox is removed. Overlay, stored boxes, and the parity harness use that frame.
3. Split the read path. `:app` reads `:data` repositories and passes domain objects into `:export`. `:export` stays on `:core-model` and does not open Room.

## Divergence coverage — thin

The level below is the epics of one Android field app (capture, camera, inference, tracking, location, data, density, review, export, packs). The spine fixes the seams those epics would otherwise invent alone: a second pipeline, a second preprocess, latest-fix joins, UTC-versus-sensor clocks, raw GPS versus canopy point, session plot copied onto events, frame counts as density, Play services on `:app`, and per-module preference stores as the idea. AD-13 closes the join timebase the memlog had left open.

What remains would let two epics satisfy every Rule and still not integrate.

### Findings

- **high** Source-frame orientation is undefined (AD-3, Rule). *Prevents* names "a second box coordinate space." The Rule only says `NormalizedBox` is "0..1 of the source frame after letterbox is removed." CameraX analysis buffers are sensor-oriented; stills in the parity harness are usually upright. A camera epic can hand the sensor buffer, an inference epic can letterbox that buffer as if it were upright, and an overlay epic can map the same 0..1 box onto the preview. All three follow the words. *Fix:* state that rotation is applied inside the one preprocess before letterbox, and that "source frame" means that upright frame. *Disposition:* autofix.
- **medium** `minTrackFrames` has two homes (AD-3 Rule list, AD-6 Rule, AD-11 Rule). The spec manifest carries `minTrackFrames`. AD-3's closed list of manifest fields omits it. AD-6 requires the threshold. AD-11 says new thresholds are fields on the config snapshot. A pack epic and a tracking epic can each own it. *Fix:* put `minTrackFrames` on the manifest list, or say the snapshot overrides it and the manifest value is only the default. *Disposition:* autofix.
- **medium** Evidence bytes are unnamed (AD-5 Rule, AD-6 Rule, AD-7 Rule). Data is the only file writer, the event has `imagePath` and `thumbPath`, and EXIF GPS matches the canopy point. Nothing says who encodes the JPEG, who makes the thumb, or who stamps EXIF. Tracking can return a finished file and data can write those bytes unchanged, so EXIF never matches the canopy point, while both epics obey the Rules. *Fix:* `:data` encodes the evidence JPEG and thumb at the single write, and stamps EXIF from the canopy point then. Numeric quality and long-side cap stay config. *Disposition:* autofix.
- **low** Spec §9 "discard or flag low-accuracy fixes" is neither a Rule nor Deferred. Smoothing and the join already sit in `:location`, so this is one module's policy, not a second contract. *Fix:* defer it with the revisit "before the location epic." *Disposition:* defer.

## AD enforceability — broken

AD-1, AD-2, AD-4, AD-6, AD-7, AD-8, AD-9, AD-12, and AD-13 name a divergence and state a check a later epic can obey or fail. AD-4's synchronous `detect` versus flows, AD-13's elapsed-realtime axis, and AD-9's classpath ban are enforceable as written. AD-7's canopy point and plot-name split matches its *Prevents* line. The failures are where the Rule is narrower than *Prevents*, or where two Rules cannot both be true.

### Findings

- **high** AD-11 does not stop the divergence it names. *Prevents:* "each module reading its own preferences." *Rule:* "`:app` loads one immutable config object" and "Pure modules do not read device storage." Pure modules are the JVM set in AD-2 (`:tracking`, `:density`, `:export`). `:camera`, `:inference`, `:location`, and `:data` may still read preferences, use those values, and leave a snapshot that does not describe the session. *Fix:* no module except `:app` reads settings storage. *Disposition:* autofix.
- **high** AD-5 and AD-2 cannot both be implemented. AD-5: "Export and review read repositories." AD-2: `:export` depends only on `:core-model`. Repositories live in `:data`, and `:data` does not depend on the port modules either. An export epic that reads repositories takes a dependency AD-2 forbids. An export epic that stays pure never reads repositories. Review can read, because review is in `:app`. *Fix:* change the AD-5 sentence so `:app` reads repositories and calls pure exporters with `:core-model` values. *Disposition:* autofix.
- **medium** AD-4 and AD-10 name two inference entries. AD-4 makes `Detector.detect` the per-frame call. AD-10 says "`InferenceBackend` is the only inference entry" to prevent `:app` calling a runtime. A capture epic can call `Detector` and a benchmark epic can call `InferenceBackend` as a peer, and both can quote an AD while one of them still reaches LiteRT, NCNN, or ONNX from `:app`. *Fix:* `Detector` is the pipeline port; its implementation is the only caller of `InferenceBackend`; `:app` does not touch a runtime. *Disposition:* autofix.
- **medium** Density results have no storage rule (AD-8 Rule, AD-5 Rule, ERD). AD-8 says live alerts and the review map read segment results and the UI does not recompute them from frames. AD-5's writer list is sessions, events, and image files. The ERD draws `DensitySegment` as a session child. A density epic can persist rows and a review epic can recompute from events through `:density`. Both obey the Rules and the schema differs. *Fix:* either segments are derived by `:density` at read time and are not a table, or `:data` is the only writer of segment rows. *Disposition:* autofix.

## Deferred — thin

Most deferrals are bounded by an AD, so two epics cannot wander off.

- Backend winner: AD-10 lists the candidates and says the benchmark picks.
- `index.json` host: the app binds to the field list and a configured URL.
- Which tiers ship in the index: import and optional download both exist.
- Benchmark media: on device, long enough to show throttling.
- Numeric thresholds: config snapshot (AD-11).
- Tile archive: one owner, the review screen's local store. A single reader can wait.
- Release omitting `:devtools`: replay types stay in the port modules (structural seed). Packaging does not fork the pipeline.
- Quantization and input size: they arrive as a pack under AD-3.
- Segmentation and RTK: later, behind `Detector` and `LocationSource`.

### Findings

- **medium** "Navigation library inside `:app`" (Deferred) has no owner and no "exactly one" clause. Capture UI and review UI are separate epics in the same module. Each can add a library and still claim the choice was deferred. *Fix:* defer "which library," and adopt "one navigation library for all of `:app`; no epic adds a second." *Disposition:* autofix.

## Named tech — adequate

Stack pins are present for every table row: Kotlin 2.4.21, AGP 9.5.0, compileSdk/targetSdk 37, minSdk 26, Compose BOM 2026.09.00, CameraX 1.6.2, Room 2.8.5, WorkManager 2.12.0, Hilt 2.60.1, kotlinx-coroutines 1.11.0, MapLibre Android SDK 13.7.1. The memlog records a 2026-11-08 check with sources. Nothing is obviously stale relative to that date (the check is the same day as this review). This walker did not re-fetch.

LiteRT, NCNN, and ONNX Runtime Mobile are named in AD-10 and unpinned. That is acceptable while the winner is deferred, provided the version catalog pins the chosen artifacts before the inference epic vendors them. Not a defect.

## Parent spine — strong

`initiative-grape-leaf.md` has `parent: none`. The spine has `binds: []` and no Inherited Invariants section. Nothing here weakens a parent AD, because there is no parent. Correct omission.

## Altitude dimensions — adequate

Initiative altitude for this app owns paradigm, module graph, shared contracts, state mutation, identity, and the operational envelope. Each is decided or explicitly deferred.

- Paradigm: pipes-and-filters, ports named, `:app` composes.
- Structure: module tree, dependency diagram, ERD.
- Contracts and state: AD-3 through AD-8, AD-11 through AD-13.
- Deployment: sideloadable APK, minSdk 26, no Play Store requirement (AD-9).
- Environments: field use with no network; debug replay through port fakes; release-versus-`:devtools` is deferred.
- Infra: no app backend; Room and images on device; tiles are a local store; optional static pack index is not required in the field (phone diagram).
- Operations: no telemetry, no auth, no network logging; background download and export on `androidx.work`, not the capture service; thermal adaptation on API 29+; SHA-256 before a downloaded pack is used.

The operational envelope is not silent. No finding.

## Template and placeholders — strong

No template comments, no `TBD` / `TODO` / `FIXME`, no unfilled tokens, no empty headings, no empty diagrams. Deferred rows are real choices. Frontmatter is filled in. No finding.
