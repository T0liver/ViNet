---
type: epic
title: Review and export
parent: initiative-grape-leaf
covers:
  - R3
  - R4
  - R5
after: []
refined: false
risk: medium
---

# Review and export

## Description

A finished session can be filtered by class, plot, and feedback on an offline map and a list. An event can be marked and opened in another maps app. The session can be shared as GeoJSON, CSV, KML/GPX, a zip, and a retraining file of events marked wrong or unsure plus events under the configured confidence. In landscape the map and the list sit side by side. For R5 the map draws flagged segments from stored rows and does not compute them. This epic adds the Sessions destination.

## Outcome

A farmer can find a stored sighting offline, mark it, and share the session through the Android share sheet.

## Done when

- The map and the list filter together by class, plot, and feedback. With no tiles, the list and export still work.
- In landscape the map and the list sit side by side.
- GeoJSON, CSV, KML/GPX, a zip of images, and the retraining file each open the share sheet, and the session stays on the phone.
- The map draws flagged segments from stored rows and does not compute density.
- A sideloaded build shows a finished session with no network.

## Boundaries

Disease pressure writes the segment rows and the scan alert. This epic does not change those rows. Packs, tiers, and heat owns the Settings row that imports tiles. This epic only reads tiles already on the device. An event with no canopy point still appears in the list.

## References

- parent — _bmad-output/initiative-grape-leaf/initiative-grape-leaf.md, Requirements R3, R4, R5
- architecture — _bmad-output/initiative-grape-leaf/architecture-grape-leaf/architecture-grape-leaf.md, AD-8, AD-9, AD-12
- experience — _bmad-output/initiative-grape-leaf/ux-grape-leaf/EXPERIENCE.md, Session storage, review, export
