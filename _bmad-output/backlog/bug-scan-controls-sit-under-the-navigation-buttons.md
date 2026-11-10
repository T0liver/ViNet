---
type: bug
title: "Scan controls sit under the phone's navigation buttons"
parent: none
covers: []
after: []
assignee: ""
refined: true
hitl: true
risk: low
severity: P2
---

# Scan controls sit under the phone's navigation buttons

## Description

On a phone, the bottom of Scan slides under the system navigation buttons. Start and Stop are part of what slips. The camera preview may stay edge to edge. Session status, the tractor control, the preview control, Start, and Stop should sit fully in the area above those buttons and below the status bar, so each one can be tapped. A person checks this on a phone after the change, because the emulator is not the check.

## Reproduction

1. Install the debug build on a phone that shows on-screen navigation buttons.
2. Open ViNet. Scan is the first screen.
3. Look at the bottom of the screen, with the session stopped and again after Start.

Actual: the bottom of the screen, including Start and Stop, sits under the navigation buttons.
Expected: the preview can reach the screen edges, and the controls sit fully above the navigation buttons.

Reported by O on 2026-11-10 after installing that debug build. The scripted box and the camera session otherwise worked.

## Cause Hypothesis

Scan fills the window and places the bottom controls with a fixed margin from the physical bottom of the screen. That margin does not grow by the system navigation inset, so the controls are measured into the navigation buttons. Hiding the system bars is only for screen-off, and day and dim still show them.

## Acceptance Criteria

1. **Scan controls clear the system bars**
   **Given** Scan is open on a phone with on-screen navigation, in day mode
   **When** the screen is shown, stopped and running
   **Then** session status, the tractor control, the preview control, Start, and Stop sit fully above the navigation buttons and below the status bar, and each can be tapped
2. **The preview stays edge to edge**
   **Given** a session is running and the preview is visible
   **When** the phone is held in portrait
   **Then** the camera preview still reaches the screen edges, and the labeled box is not covered by the navigation buttons
3. **Screen off still hides the bars**
   **Given** a session is running with the screen off
   **When** the tractor control is on screen off
   **Then** the system bars hide as they do now, and leaving screen off shows the controls clear of the bars again
4. **Tests cover the condition found and fixed**
   **Given** the test suite
   **When** it runs
   **Then** a test that fails on the overlap and passes on the fix covers the reproduction, and one covers screen off still hiding the bars
5. **Or: no change is needed, with proof**
   **Given** the reproduction
   **When** it is run on the current code
   **Then** the expected behavior already holds, or the report was mistaken, with the evidence recorded in Notes — this supersedes 1 through 4

## References

- parent — none
- design — _bmad-output/initiative-grape-leaf/ux-grape-leaf/DESIGN.md, Layout & Spacing
- phone report — O, 2026-11-10, debug build on a physical phone

## Notes

- Decision: 2026-11-10 Filed from the phone check of story 2.4. It is not part of stories 2.4, 2.5, 2.6, or 2.7.
- Decision: 2026-11-10 O said to build it in this session and close it when the check passes. The JVM tests passed. The phone look of this fix is still O's to confirm on the new debug build.
- Assumption: the buttons O saw are the system navigation buttons, not an in-app bar. The design line "Content clears the bottom navigation" is the rule this applies to Scan.
- Risk is low because the session still runs. Severity is P2 because Start and Stop are harder to reach, and enough of a button can remain tappable.
