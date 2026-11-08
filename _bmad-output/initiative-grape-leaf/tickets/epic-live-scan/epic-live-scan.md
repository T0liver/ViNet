---
type: epic
title: Live scan
parent: initiative-grape-leaf
covers:
  - R1
  - R7
after: []
refined: false
risk: high
---

# Live scan

## Description

The foreground service is the only capture pipeline. It publishes upright frames and encodes the full frame and thumbnail before the camera buffer is released. A debug build includes a stand-in pack and may include a video for the first test. A release build includes no model file. A session shows boxes labeled with the pack class and score, from the camera or from video replay, and keeps running with the preview hidden or the screen off. The navigation shell starts here, with Scan as its first destination. Sessions and Settings are added by later epics. The spike starts on LiteRT. For R7, video replay, GPX replay, the scripted detector, the debug HUD, and the stand-in parity check run on the debug build only.

## Outcome

On a debug build, a farmer or developer sees labeled boxes on the canopy and the scan keeps running with the screen off.

## Done when

- One foreground-service pipeline shows boxes labeled with the pack class and score.
- Hiding the preview or turning the screen off leaves the session running.
- A debug build replays a video and a GPX file through that pipeline, shows the debug HUD, and passes the stand-in parity check.
- The release build includes no model file, no debug HUD, and no parity hooks.
- A sideloaded debug build shows this on a device or emulator.

## Boundaries

Events and place owns tracks, merges, and stored events. Packs, tiers, and heat owns the release pack install, the benchmark, and Settings. This epic starts the navigation shell with Scan only. The spike's first runtime is LiteRT, and the field default is still the later benchmark.

## References

- parent — _bmad-output/initiative-grape-leaf/initiative-grape-leaf.md, Requirements R1, R7
- architecture — _bmad-output/initiative-grape-leaf/architecture-grape-leaf/architecture-grape-leaf.md, AD-1, AD-3, AD-4, AD-10
- experience — _bmad-output/initiative-grape-leaf/ux-grape-leaf/EXPERIENCE.md, Real-time on-device detection
