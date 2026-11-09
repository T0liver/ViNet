---
type: epic
title: Phone proof
parent: initiative-grape-leaf
covers:
  - R1
  - R2
  - R3
  - R4
  - R5
  - R6
after:
  - epic-packs
refined: true
risk: high
---

# Phone proof

## Description

The release build, which contains no model file, demonstrates R1, R2, R3, R4, R5, and R6 on a high-end phone, an S10 with no Play services, and a low-end phone, handheld and mounted, after a pack is imported or downloaded. Config numbers may change. Behavior does not.

## Outcome

The same release build has been shown scanning, storing, reviewing, alerting, and loading a pack on the three named phones, in the hand and on a mount.

## Requirements

- R1 (parent R1): The release build shows a live labeled scan on a high-end phone, an S10 with no Play services, and a low-end phone, in the hand and on a mount. Met when each phone has done both postures.
- R2 (parent R2): Those same runs store one photo and one canopy point per new sighting. Met when each phone shows a stored event after the scan.
- R3 (parent R3): Those same runs open the session on the map and in the list. Met when each phone can filter and open one event with no network.
- R4 (parent R4): Those same runs share one export through the Android share sheet. Met when each phone opens the share sheet and keeps the session.
- R5 (parent R5): A moving scan on each phone shows "high disease pressure here", and a parked phone does not. Met when each phone shows that contrast.
- R6 (parent R6): Each phone scans only after a pack is imported or downloaded, and the release APK contains no model file. Met when a local import is enough on each phone.

## Constraints

- C2 (AD-9): The S10 run has no Play services on the device. Config numbers may change. Behavior does not.

## Done when

- A high-end phone, an S10 with no Play services, and a low-end phone have each run a handheld scan and a mounted scan.
- Each run uses the release APK, which contains no model file, after a pack is imported or downloaded.
- The scan, the event, the review, the pressure alert, and the tier choice match the requirements. Config numbers may change.
- The S10 run has no Play services on the device.

## Boundaries

This epic does not add behavior. Real model swap owns the trained pack. A person runs the three phones.

## References

- parent — _bmad-output/initiative-grape-leaf/initiative-grape-leaf.md, Requirements R1, R2, R3, R4, R5, R6
- architecture — _bmad-output/initiative-grape-leaf/architecture-grape-leaf/architecture-grape-leaf.md, AD-9

## Notes

- Waits on Packs, tiers, and heat because: the release pack path and the field behaviors have to be in the build a person carries to the phones.
- Decision: 2026-11-09 The tracer is the high-end phone, handheld and mounted, because it is the first full release path. The S10 with no Play services is next, because a hidden Play dependency is the least certain part. A person runs every phone.
