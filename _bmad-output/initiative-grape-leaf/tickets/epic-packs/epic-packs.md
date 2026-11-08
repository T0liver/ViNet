---
type: epic
title: Packs, tiers, and heat
parent: initiative-grape-leaf
covers:
  - R2
  - R6
after: []
refined: false
risk: medium
---

# Packs, tiers, and heat

## Description

The release APK contains no model. A local import or a checksummed Wi-Fi download from a configured index supplies a pack, and neither adds a Play services dependency. The benchmark loops still images that ship on the device and selects the largest tier that holds the frame rate. The farmer can override that tier for the next session. If the smallest tier cannot hold the rate, the app says so and still opens a scan. On API 29 and above, heat can slow inference. This epic adds the Settings destination. For R2, Settings imports plot polygons once for later sessions. Settings also holds the storage meter, image cleanup, and the tile-import row.

## Outcome

A release install cannot scan until the farmer imports a pack or downloads one, and the benchmark then picks a tier from still images already on the device.

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
