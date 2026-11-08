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
refined: false
risk: high
---

# Phone proof

## Description

The release build, which contains no model file, demonstrates R1, R2, R3, R4, R5, and R6 on a high-end phone, an S10 with no Play services, and a low-end phone, handheld and mounted, after a pack is imported or downloaded. Config numbers may change. Behavior does not.

## Outcome

The same release build has been shown scanning, storing, reviewing, alerting, and loading a pack on the three named phones, in the hand and on a mount.

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
