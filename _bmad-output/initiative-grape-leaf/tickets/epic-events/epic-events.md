---
type: epic
title: Events and place
parent: initiative-grape-leaf
covers:
  - R2
after: []
refined: true
risk: high
---

# Events and place

## Description

A sighting becomes one stored event with one full-frame photo and one thumbnail. A fix inside the accuracy cap is stored as the canopy point. A fix outside it leaves the event with no canopy point and the words "location is missing". A stored fix worse than 5 m is labeled "Weak GPS". A nearby repeat of the same class does not create another event and keeps the existing photo. The image store enforces the session and global caps, can skip the files below the confidence setting, and can delete old files while keeping the rows. Session status shows the storage meter when a cap is nearly full. Plot polygons already on the device set `event.plotName`. The session plot label is not copied onto events.

## Outcome

A scan stores one photo and one row per sighting, with a canopy point only when a fix was inside the accuracy cap.

## Requirements

- R1 (parent R2): Each new sighting becomes one stored event with class, confidence, and at most one full-frame photo and one thumbnail. A fix inside `maxAccuracyM` is stored as the canopy point, with accuracy, speed, and bearing. A stored fix worse than `weakAccuracyM`, seeded at 5 m, is labeled "Weak GPS". When every fix is outside the cap, the event is still stored, with its photo and no canopy point, and the screen says "location is missing". The same class within the configured distance and time stays that event and keeps its photo. Met when a known replay writes one row and one image, a nearby repeat does not write a second row, and a replay with no fix inside the cap writes the row with no canopy point.

## Constraints

- C4 (SPEC §3.5, AD-4): A track ends only when its configured max age is exceeded, not because a frame had no detections.
- C8 (AD-5, AD-6): Tracking decides create, merge, and the best frame. Data is the only writer. A merge keeps the existing image.
- C9 (AD-7, AD-13): The stored position is the canopy point after the mount offset. The frame and the fix join on elapsed realtime. A fix worse than `maxAccuracyM` is excluded from the join and from distance.
- C13 (AD-12): Data mints one opaque id per event. Stored instants are UTC epoch milliseconds.

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

## Notes

- Decision: 2026-11-09 The tracer is "One track writes one event": a replay raises the session count to one and leaves one photo. The frame-to-canopy join is next, because that clock and the mount offset are the least certain part. The visible proof until Review exists is the session count and the image file.
