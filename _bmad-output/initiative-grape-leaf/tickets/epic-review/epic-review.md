---
type: epic
title: Review and export
parent: initiative-grape-leaf
covers:
  - R3
  - R4
  - R5
after: []
refined: true
risk: medium
---

# Review and export

## Description

A finished session can be filtered by class, plot, and feedback on an offline map and a list. An event can be marked and opened in another maps app. The session can be shared as GeoJSON, CSV, KML/GPX, a zip, and a retraining file of events marked wrong or unsure plus events under the configured confidence. In landscape the map and the list sit side by side. For R5 the map draws flagged segments from stored rows and does not compute them. This epic adds the Sessions destination.

## Outcome

A farmer can find a stored sighting offline, mark it, and share the session through the Android share sheet.

## Requirements

- R1 (parent R3): The farmer can open a finished session on an offline map and in a list, filter by class, plot, and feedback, mark an event correct, wrong, or unsure, and open its canopy point in another maps app or read bearing and distance. Met when this works with no network, and with no tiles the list still works.
- R2 (parent R4): The farmer can export that session as GeoJSON, CSV, KML/GPX, a zip of images, and a retraining file, and hand the file off through the Android share sheet. The retraining file contains events marked wrong or unsure, plus events under the configured confidence. The session stays on the phone. Met when each format is produced and the share sheet opens.
- R3 (parent R5): The review map flags a segment with density and severity as separate values. Met when a stored flagged row draws on the map. This epic does not compute the flag.

## Constraints

- C1 (SPEC §3.1, AD-9): Review and export work with no network. There is no account and no sync.
- (AD-9): The review map is MapLibre drawing tiles already on the device. Navigate-to-spot is a geo intent.

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

## Notes

- Decision: 2026-11-09 The tracer is "Open one stored sighting" in the session list. The offline map is next, because MapLibre and missing tiles are the least certain part. The start sheet stays out of this epic.
