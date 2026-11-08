---
type: epic
title: Disease pressure
parent: initiative-grape-leaf
covers:
  - R5
after: []
refined: false
risk: medium
---

# Disease pressure

## Description

A moving scan writes flagged segment rows and shows the sentence "high disease pressure here" on the scan and on the service notification when the screen is off. That notification vibrates once. A stationary scan does not. This epic does not edit the review map.

## Outcome

A farmer who is moving hears and sees "high disease pressure here" when the recent distance crosses the threshold, and a parked phone does not raise that alert.

## Done when

- A moving replay that crosses the threshold shows the sentence and writes a flagged segment row.
- With the screen off, the same sentence is the service notification and it vibrates once.
- A stationary replay adds no coverage and does not alert.
- The review map, already built, draws those rows. This epic does not change the map.
- A sideloaded build shows the alert during a scan.

## Boundaries

Review and export owns the map, including how a flagged segment is drawn. Events and place owns the event rows this epic counts. Density and severity stay separate values.

## References

- parent — _bmad-output/initiative-grape-leaf/initiative-grape-leaf.md, Requirements R5
- architecture — _bmad-output/initiative-grape-leaf/architecture-grape-leaf/architecture-grape-leaf.md, AD-8
- experience — _bmad-output/initiative-grape-leaf/ux-grape-leaf/EXPERIENCE.md, Density and severity warnings
