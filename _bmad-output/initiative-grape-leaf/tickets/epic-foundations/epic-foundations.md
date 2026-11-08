---
type: epic
title: Foundations
parent: initiative-grape-leaf
covers:
  - R2
  - R5
  - R7
after: []
refined: true
risk: medium
---

# Foundations

## Description

The modules, shared contracts, manifest parser, Room schema, and CI are in place, and a JVM test run passes. The app id is `hu.toliver.vinet`, and the launcher shows the grape-leaf mark and the ViNet wordmark. For R2 and R5 this is the schema only, including the segment table. For R7 this is the JVM test run only.

## Outcome

A developer can install a sideloaded build whose launcher shows the mark, and CI runs the JVM tests.

## Requirements

- R1 (parent R2): Each new sighting becomes one event with class, confidence, and at most one full-frame photo and one thumbnail. A fix inside `maxAccuracyM` is stored as the canopy point, with accuracy, speed, and bearing. A stored fix worse than `weakAccuracyM`, seeded at 5 m, is labeled "Weak GPS". When every fix is outside the cap, the event is still stored, with its photo and no canopy point, and the screen says "location is missing". The same class within the configured distance and time stays that event and keeps its photo. Met when a known replay writes one row and one image, a nearby repeat does not write a second row, and a replay with no fix inside the cap writes the row with no canopy point.
- R2 (parent R5): While the farmer is moving, the app shows the sentence "high disease pressure here" when events per meter cross the configured threshold, and the review map flags that segment with density and severity as separate values. With the screen off, that sentence is the service notification, and the notification vibrates once. A scan that is not moving does not raise the alert. Met when a moving replay crosses the threshold and a stationary one does not.
- R3 (parent R7): On a debug build, a developer can point the same scan path at a video and a GPX file, use a stand-in or scripted detector, and read the debug figures. A fixed image set run in the app matches a reference run closely enough to catch preprocess mismatches. The release build does not include the stand-in model, the replay media, the debug HUD, or the parity hooks. Met when a debug replay yields the same kind of overlay and events as a camera session and the stand-in parity check passes.

## Constraints

- C2 (SPEC §3.2, §3.3, AD-9): The app has no Play services dependency, including transitively, and the APK is sideloaded. Fused location is not a dependency of the app.
- C3 (SPEC §3.4, architecture stack): minSdk is 26 and targetSdk is 37.
- C5 (SPEC §3.6, AD-3, DESIGN): Classes, colors, score thresholds, and preprocessing come only from the model-pack manifest. Class color is not a theme token.
- C7 (AD-2): core-model, tracking, density, and export import no Android APIs. Inference, camera, location, and data depend on core-model and not on each other.
- C11 (AD-11): The app passes one immutable config snapshot into the other modules, and the session stores that snapshot. The key names are fixed, including `weakAccuracyM`. The numbers are tuned on phones.
- C12 (AD-10): Inference goes through InferenceBackend, and only bundled runtimes are allowed. The benchmark loops still images that ship on the device and does not download media. A release APK contains no model file. A debug APK may bundle a stand-in model and a video for the first test. Thermal adaptation runs on API 29 and above and does nothing below.
- C13 (AD-12): Data mints one opaque id per session and per event. Stored instants are UTC epoch milliseconds. Exports render those instants as ISO-8601 UTC.
- C14 (SPEC §1, §19, DESIGN): The app is ViNet, package `hu.toliver.vinet`. The mark is the grape-leaf drawing, chrome is vine and sky, and the wordmark is Fraunces.

## Done when

- The module graph matches the architecture spine, and CI runs the JVM tests.
- Session, event, and segment tables exist. No scan writes rows yet.
- The launcher shows the grape-leaf mark and the ViNet wordmark.
- A sideloaded build installs on a device or emulator.

## Boundaries

Live scan owns the capture pipeline and the navigation library. Events and place owns writing events and images. This epic does not run the camera, choose a tier, or create a sighting. R1 and R2 are the records the schema must be able to hold. R3 is the JVM test run, not the replay itself.

## References

- parent — _bmad-output/initiative-grape-leaf/initiative-grape-leaf.md, Requirements R2, R5, R7
- architecture — _bmad-output/initiative-grape-leaf/architecture-grape-leaf/architecture-grape-leaf.md, AD-2, AD-11, Stack
- design — _bmad-output/initiative-grape-leaf/ux-grape-leaf/DESIGN.md, Leaf mark and Wordmark

## Notes

- Decision: 2026-11-08 Refined from the initiative lines and the architecture spine. The CI vendor is left to the first story. The navigation library is left to Live scan.
