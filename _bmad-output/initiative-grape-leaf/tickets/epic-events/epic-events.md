---
type: epic
title: Events and place
parent: initiative-grape-leaf
covers:
  - R2
after: []
refined: false
risk: high
---

# Events and place

## Description

A sighting becomes one stored event with one full-frame photo and one thumbnail. A fix inside the accuracy cap is stored as the canopy point. A fix outside it leaves the event with no canopy point and the words "location is missing". A stored fix worse than 5 m is labeled "Weak GPS". A nearby repeat of the same class does not create another event and keeps the existing photo. The image store enforces the session and global caps, can skip the files below the confidence setting, and can delete old files while keeping the rows. Session status shows the storage meter when a cap is nearly full. Plot polygons already on the device set `event.plotName`. The session plot label is not copied onto events.

## Outcome

A scan stores one photo and one row per sighting, with a canopy point only when a fix was inside the accuracy cap.

## Done when

- A known replay writes one event, one full-frame photo, and one thumbnail.
- A nearby repeat of the same class does not write a second row or a second photo.
- A replay with no fix inside the cap stores the event with no canopy point, and the screen says "location is missing".
- A stored fix worse than `weakAccuracyM` is labeled "Weak GPS". The event is not discarded.
- Crossing a storage cap keeps the row and skips the new photo. Cleanup deletes files and keeps rows.
- A sideloaded debug build shows the stored event after a scan.

## Boundaries

Packs, tiers, and heat imports plot polygons once in Settings. This epic only tags events from polygons already stored. Review and export draws the map and the list. Disease pressure counts events and does not write them.

## References

- parent — _bmad-output/initiative-grape-leaf/initiative-grape-leaf.md, Requirements R2
- architecture — _bmad-output/initiative-grape-leaf/architecture-grape-leaf/architecture-grape-leaf.md, AD-5, AD-6, AD-7, AD-13
- experience — _bmad-output/initiative-grape-leaf/ux-grape-leaf/EXPERIENCE.md, Tracking and event logging
