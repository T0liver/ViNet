---
type: epic
title: Live scan
parent: initiative-grape-leaf
covers:
  - R1
  - R7
after: []
refined: true
risk: high
---

# Live scan

## Description

The foreground service is the only capture pipeline. It publishes upright frames and encodes the full frame and thumbnail before the camera buffer is released. A debug build includes a stand-in pack and may include a video for the first test. A release build includes no model file. A session shows boxes labeled with the pack class and score, from the camera or from video replay, and keeps running with the preview hidden or the screen off. The navigation shell starts here, with Scan as its first destination. Sessions and Settings are added by later epics. The spike starts on LiteRT. For R7, video replay, GPX replay, the scripted detector, the debug HUD, and the stand-in parity check run on the debug build only.

## Outcome

On a debug build, a farmer or developer sees labeled boxes on the canopy and the scan keeps running with the screen off.

## Requirements

- R1 (parent R1): A farmer can scan a canopy on foot or from a mount and see the latest boxes labeled with the pack class and score, and the scan keeps running when the preview is hidden or the screen is off. Met when a running session shows those boxes and still runs after the preview is hidden or the screen is off.
- R2 (parent R7): On a debug build, a developer can point the same scan path at a video and a GPX file, use a stand-in or scripted detector, and read the debug figures. A fixed image set run in the app matches a reference run closely enough to catch preprocess mismatches. The release build does not include the stand-in model, the replay media, the debug HUD, or the parity hooks. Met when a debug replay shows the same kind of overlay as a camera session and the stand-in parity check passes. Stored events belong to Events and place.

## Constraints

- C4 (SPEC §3.5, AD-4): The model favors precision, so a track ends only when its configured max age is exceeded, not because a frame had no detections. This epic's detector returns an empty list for a gap and does not invent boxes.
- C5 (SPEC §3.6, AD-3, DESIGN): Classes, colors, score thresholds, and preprocessing come only from the model-pack manifest. Class color is not a theme token.
- C6 (AD-1): The foreground service is the only capture pipeline. Replay and the scripted detector are port implementations. The UI does not detect, own tracks, or write events.
- C12 (AD-10): The spike starts on LiteRT. A debug build may bundle a stand-in model and a video for the first test. A release build includes no model file, no replay media, no debug HUD, and no parity hooks.

## Done when

- One foreground-service pipeline shows boxes labeled with the pack class and score.
- Hiding the preview or turning the screen off leaves the session running.
- A debug build replays a video and a GPX file through that pipeline, shows the debug HUD, and passes the stand-in parity check.
- The release build includes no model file, no debug HUD, and no parity hooks.
- A sideloaded debug build shows this on a device or emulator.

## Boundaries

Events and place owns tracks, merges, stored events, and the canopy join. Packs, tiers, and heat owns the release pack install, the benchmark, and Settings. This epic starts the navigation shell with Scan only, using Jetpack Navigation Compose. Sessions and Settings are later destinations. GPX replay emits fixes. It does not compute the canopy point.

## References

- parent — _bmad-output/initiative-grape-leaf/initiative-grape-leaf.md, Requirements R1, R7
- architecture — _bmad-output/initiative-grape-leaf/architecture-grape-leaf/architecture-grape-leaf.md, AD-1, AD-3, AD-4, AD-10
- experience — _bmad-output/initiative-grape-leaf/ux-grape-leaf/EXPERIENCE.md, Real-time on-device detection

## Notes

- Decision: 2026-11-09 Refined from the initiative lines, the architecture spine, and the experience spine. The navigation shell uses Jetpack Navigation Compose, chosen once in the app module. The field runtime stays with the later benchmark. LiteRT is only the spike's first implementation.
- Decision: 2026-11-09 The tracer is "See one scripted box": Scan, the foreground service, and one labeled box from a scripted detector. LiteRT on a still image is next, because the real stand-in and its preprocess are the least certain part. Camera, screen-off, and replay fill the same service after that. The debug HUD shows FPS, inference latency, and temperature. Open tracks wait for Events and place. This epic's Scan screen is start, stop, status, the overlay, the preview control, and the tractor control. The start sheet's pack, mount, plot, and notes wait for later epics.
