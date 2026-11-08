---
type: epic
title: Foundations
parent: initiative-grape-leaf
covers:
  - R2
  - R5
  - R7
after: []
refined: false
risk: medium
---

# Foundations

## Description

The modules, shared contracts, manifest parser, Room schema, and CI are in place, and a JVM test run passes. The app id is `hu.toliver.vinet`, and the launcher shows the grape-leaf mark and the ViNet wordmark. For R2 and R5 this is the schema only, including the segment table. For R7 this is the JVM test run only.

## Outcome

A developer can install a sideloaded build whose launcher shows the mark, and CI runs the JVM tests.

## Done when

- The module graph matches the architecture spine, and CI runs the JVM tests.
- Session, event, and segment tables exist. No scan writes rows yet.
- The launcher shows the grape-leaf mark and the ViNet wordmark.
- A sideloaded build installs on a device or emulator.

## Boundaries

Live scan owns the capture pipeline. Events and place owns writing events and images. This epic does not run the camera, choose a tier, or create a sighting.

## References

- parent — _bmad-output/initiative-grape-leaf/initiative-grape-leaf.md, Requirements R2, R5, R7
- architecture — _bmad-output/initiative-grape-leaf/architecture-grape-leaf/architecture-grape-leaf.md, AD-2, AD-11, Stack
- design — _bmad-output/initiative-grape-leaf/ux-grape-leaf/DESIGN.md, Leaf mark and Wordmark
