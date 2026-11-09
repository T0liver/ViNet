---
type: epic
title: Packs, tiers, and heat
parent: initiative-grape-leaf
covers:
  - R2
  - R6
after: []
refined: true
risk: medium
---

# Packs, tiers, and heat

## Description

The release APK contains no model. A local import or a checksummed Wi-Fi download from a configured index supplies a pack, and neither adds a Play services dependency. The benchmark loops still images that ship on the device and selects the largest tier that holds the frame rate. The farmer can override that tier for the next session. If the smallest tier cannot hold the rate, the app says so and still opens a scan. On API 29 and above, heat can slow inference. This epic adds the Settings destination. For R2, Settings imports plot polygons once for later sessions. Settings also holds the storage meter, image cleanup, and the tile-import row.

## Outcome

A release install cannot scan until the farmer imports a pack or downloads one, and the benchmark then picks a tier from still images already on the device.

## Requirements

- R1 (parent R2): Plot polygons are imported once in Settings and reused by later sessions. Met when a second session tags events from that file without asking again. This epic does not run point-in-polygon.
- R2 (parent R6): A release APK contains no model file. The farmer imports a pack from local storage with no network, or downloads one from a configured index on Wi-Fi, and a download is installed only after the checksum matches. The app selects the largest installed tier that holds the configured frame rate, and the farmer can override that choice for the next session. If the smallest tier cannot, the app says so and still opens a scan. Met when a release install cannot scan until a pack is imported or downloaded, a bad checksum leaves the previous pack in place, and a local import is enough.

## Constraints

- C2 (SPEC §3.2, AD-9): Import and download add no Play services dependency.
- C12 (AD-10): The benchmark loops still images that ship on the device. On API 29 and above, heat may slow inference or offer a lighter installed tier. Below API 29 it does not. The host is a configured URL. GitHub is allowed and is not required.

## Done when

- The release APK contains no model file. A local import is enough to scan, with no network.
- A download installs only after the checksum matches, and a failure leaves the previous pack in place.
- The benchmark loops shipped still images, picks the largest tier that holds the frame rate, and allows an override for the next session.
- If the smallest tier cannot hold the rate, the app says so and still opens a scan.
- On API 29 and above, heat slows inference or offers a lighter installed tier. Below API 29 it does not.
- Settings imports plot polygons once, imports offline tiles, shows the storage meter, and confirms image cleanup.
- A sideloaded release build completes an import and then a scan, with no Play services.

## Boundaries

Events and place tags events from polygons already stored. This epic does not run point-in-polygon. Review and export reads tiles and does not download them. The pack host is a configured URL. GitHub is allowed and is not required. The field runtime is whatever the benchmark selects. LiteRT is only the spike's first implementation.

## References

- parent — _bmad-output/initiative-grape-leaf/initiative-grape-leaf.md, Requirements R2, R6
- architecture — _bmad-output/initiative-grape-leaf/architecture-grape-leaf/architecture-grape-leaf.md, AD-9, AD-10, AD-11
- experience — _bmad-output/initiative-grape-leaf/ux-grape-leaf/EXPERIENCE.md, Model tiers

## Notes

- Decision: 2026-11-09 The tracer is "Import a pack from local storage" on a release build with no bundled model. The checksummed download is next, because the index and the retry are the least certain part. The benchmark uses the looped stills already decided.
