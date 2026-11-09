---
type: epic
title: Real model swap
parent: initiative-grape-leaf
covers:
  - R8
after:
  - epic-live-scan
  - epic-phone-proof
refined: true
risk: high
---

# Real model swap

## Description

A trained grape-leaf pack replaces the stand-in, passes the parity harness the live-scan epic built, and passes a field comparison against ground truth. It cannot start until the trainer project hands over a pack.

## Outcome

A trained pack is the active pack on a real row, the parity check passes, and a field comparison against ground truth accepts it.

## Requirements

- R1 (parent R8): A trained grape-leaf pack can replace the stand-in, pass the same parity check, and be accepted by a field comparison against ground truth. Met when that pack is the active pack on a real row and parity passes. It waits on the trainer project.

## Constraints

- C5 (AD-3): Classes, colors, thresholds, and preprocessing still come only from that pack's manifest.
- C12 (AD-10): The pipeline does not change. The pack is imported. It is not added to the release APK.

## Done when

- The trained pack is imported into the release build and selected for a scan.
- The parity check passes on that pack.
- A field comparison against ground truth accepts the result.
- A sideloaded release build runs that pack in the vineyard, with no Play services.

## Boundaries

Training, augmentation, and accuracy work stay in the trainer project. This epic does not change the capture pipeline. It uses the parity harness Live scan already built.

## References

- parent — _bmad-output/initiative-grape-leaf/initiative-grape-leaf.md, Requirements R8
- architecture — _bmad-output/initiative-grape-leaf/architecture-grape-leaf/architecture-grape-leaf.md, AD-3, AD-10

## Notes

- Waits on Live scan because: the parity harness has to exist.
- Waits on Phone proof because: the field build has to be proved before the trained pack is accepted on it.
- Unknown: the trainer project has not handed over a pack. This epic does not start until that pack exists.
- Decision: 2026-11-09 The tracer is importing that pack into the proved release build. Parity on the real pack is next, because the export layout is the least certain part. Every story waits on the pack.
