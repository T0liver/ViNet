---
name: ViNet
status: final
sources:
  - docs/SPEC.md
  - ../architecture-grape-leaf/architecture-grape-leaf.md
updated: 2026-11-08
---

# ViNet — Experience Spine

`DESIGN.md` is the visual identity. This file is how the Android app behaves. The spec's decisions are the contract. Spines win on conflict with any later mock. Lines marked [ASSUMPTION] are not in the spec or the architecture rules.

## Foundation

ViNet, package `hu.toliver.vinet`, is one Android phone app, minSdk 26, Jetpack Compose, Material 3 layout under the tokens in `DESIGN.md`. The app is sideloaded and has no Play services dependency. The leaf mark and the wordmark ViNet appear on the launcher and on Field advisory.

The farmer uses it in two postures: in the hand while walking the canopy, and fixed to a tractor or similar mount with the camera facing the canopy from about 0.5 to 1.5 m. Both postures are the same Scan surface. Stored latitude and longitude are the canopy point after the mount offset. Tractor mode dims or turns the screen off, and the service holds a wake lock either way. The UI does not detect sunlight.

The UI watches that service. It does not run the detector, own tracks, or write sessions, events, or images. Review and export read stored rows. Feedback updates the existing event row. Density and severity on screen are the segment results from the density engine. The UI does not recompute them from frames.

A model pack supplies classes, colors, score thresholds, and preprocessing. The APK may include a stand-in or the smallest tier. Field work runs offline. The only network use is an optional, user-visible pack download.

## Information Architecture

| Surface | Reached from | Purpose |
|---|---|---|
| Field advisory | First launch | Power, sun, and mount advice before any scan |
| Packs | Field advisory, Settings | Installed packs, the active pack, local import, or an optional download from the hosted index |
| Benchmark | After a pack is installed, Settings | Pick the largest tier that holds the frame-rate target |
| Scan | Navigation, after a pack exists | Live detection, session start and stop, tractor mode |
| Sessions | Navigation | Past sessions and the start of a new one |
| Session review | Session row | Map, list, density flags, export |
| Event detail | Event row or map marker | Evidence frame, feedback, navigate |
| Settings | Navigation | Mount, thresholds, storage, tiles, tier, developer tools |

[ASSUMPTION] Bottom navigation is Scan, Sessions, and Settings. The selected tab uses `{colors.sky}`. Packs and Benchmark are steps and also Settings destinations. Session review and Event detail stack on Sessions. One sheet at a time. No mockups or wireframes were produced.

## Voice and Tone

Microcopy. Brand posture is in `DESIGN.md`.

| Do | Don't |
|---|---|
| "high disease pressure here" | A softer or shorter paraphrase of that alert |
| "Import a model pack to scan. A download is optional." | "You're offline" as a block on field work |
| "This pack failed its checksum and was not installed." | A generic network error after a bad file |
| "This phone cannot hold the minimum frame rate on the smallest model." | A silent fallback to a tier that misses the frame-rate target |
| "Screen off. Scanning continues." | A claim that the phone is finished or idle |
| "Image storage is full. New events keep their place. New photos wait until you free space." | Dropping the event because the photo cannot be stored |
| "Weak GPS" | Hiding accuracy when the pipeline kept the fix |
| "No offline map tiles on this device." | An empty map with no explanation |
| Class name from the manifest | A hard-coded disease name |

[ASSUMPTION] The sentences in this table, other than "high disease pressure here", are written here because the spec requires the outcome and does not fix the words.

## Component Patterns

Behavioral rules. Visual specs are in `DESIGN.md` Components.

| Component | Use | Behavioral rules |
|---|---|---|
| Detection overlay | Scan | Draws the latest service boxes in post-letterbox normalized space. Label is manifest class name plus score. Preview may be hidden. Overlay hides with the preview. Processing continues either way. |
| Session status | Scan | Reflects the foreground service: idle, running, or stopped. Shows the active pack id and model version. Shows GPS as waiting, live, or weak. Shows Storage meter in the last tenth of the session cap or the global cap. |
| Pressure alert | Scan, and the service notification | Appears when the latest segment crosses the configured density threshold. Text is exactly "high disease pressure here". Stays until the next segment is under the threshold. With the screen off, the same sentence is the service notification text. |
| Debug HUD | Scan | Hidden unless the developer setting is on. Shows FPS, inference latency, temperature, and open tracks from the service. |
| Session row | Sessions | Tap opens Session review. Shows start, session plot label, event count, pack version. |
| Review map | Session review | Markers at the stored canopy point, one per event, colored by that event's class. The active pack's classes define those colors, the overlay labels, the swatches, and List filter. Flagged high-density segments draw from stored segment results. A parked phone adds no coverage while speed stays under the configured threshold, so it does not flag a segment. Heatmap is a toggle. Default is markers. Pan and zoom stay on the device. Tiles are whatever is already stored. |
| Event row | Session review | Thumbnail from `thumbPath` when the file exists. Otherwise the class swatch and the words "No photo". Tap opens Event detail. A merge into an existing event does not add a second photo or a second row. |
| Event detail | Event detail | Shows the full frame from `imagePath` when the file exists. Draws the stored box. Shows class, score, time, canopy position, accuracy, speed, bearing, plot name from point-in-polygon, pack id, and model version. |
| Feedback control | Event detail | Four stored values: none, correct, wrong, unsure. The control offers correct, wrong, and unsure. Choosing one writes that value on the existing row. Choosing the active value again returns it to none. |
| Storage meter | Session status, Settings | Reads the image store against the session cap and the global cap. Always visible on Settings. On Session status, visible in the last tenth of either cap. Crossing a cap stops new image files. Event rows still record. |
| Pack transfer | Packs | Lists installed packs and the active one. Import copies a local pack, and the user can select that pack with no network. Download, when the user starts it, uses the hosted index, shows progress and retry, then verifies SHA-256 before the pack can be selected. A failed check leaves the previous pack in place. |
| Benchmark panel | Benchmark | Runs on images already on the device, after the first successful install and again from Settings. Shows progress. On success, selects the largest tier that held the frame-rate target without throttling. The user can override the tier in Settings. If the smallest tier misses the frame-rate target, "This phone cannot hold the minimum frame rate on the smallest model." stays on Session status. |
| Thermal notice | Scan | On API 29 and above, when thermal status rises, the service slows inference or offers a lighter installed tier. The notice says which one happened. Below API 29 the notice is absent and the service does not adapt. |
| Field advisory | Field advisory | Leaf mark and Wordmark sit above four facts, each one sentence: bring a power bank, keep the phone out of direct sun, start with a cool phone, use a ventilated mount. Continue moves to Packs. |
| Settings row | Settings | One row, one setting. Values are the config snapshot the next session will store. The running session keeps the snapshot it started with. |
| Export sheet | Session review | Choices: GeoJSON, CSV, KML/GPX, zip of images, retraining set. [ASSUMPTION] The retraining set is events marked wrong or unsure, plus events under the configured confidence. Share opens the Android share sheet with the finished file. Leaving the surface does not cancel the job. |
| Navigate action | Event detail | Primary action opens a geo intent for the canopy point. Secondary line shows bearing and distance from the latest fix while location is on. |
| List filter | Session review | Filters the list and the markers together. Choices are class, plot name, and feedback. An empty result means no events match. Clearing the filter shows the session again. |
| Tractor control | Scan | Cycles day screen, dim, and screen off. Dim and screen off keep the service running. Screen off shows the service notification. |
| Start sheet | Scan | Confirms pack id, model version, mount side, and offset. Accepts the session plot label and notes. Confirm starts the service with a new config snapshot. |
| Leaf mark | Launcher, Field advisory | Shows `docs/vinet.svg`: leaf half and node-net half together. Colors come from the leaf ramp and sky in `DESIGN.md`. The app does not redraw or crop the mark. |
| Wordmark | Launcher, Field advisory | The text ViNet in the wordmark face. It is the app name, not a control. |

## State Patterns

| State | Surface | Treatment |
|---|---|---|
| First launch | Field advisory | Advisory, then continue. Shown once. |
| No pack | Scan, Packs | Scan does not open the camera. Copy routes to Packs. Import is available offline. Download stays disabled until a network is available, and the control shows that download is optional. |
| Checksum failure | Packs | "This pack failed its checksum and was not installed." Retry import or download. Previous pack remains selectable. |
| Benchmark running | Benchmark | Progress. Cancel returns to the previous tier choice. |
| Benchmark pass | Benchmark | Names the tier and the frame-rate target it held. Continue reaches Scan. |
| Benchmark fail | Benchmark | "This phone cannot hold the minimum frame rate on the smallest model." Scan can still be opened. Session status keeps that sentence. |
| Camera denied | Scan | "Camera is required to scan the canopy." Action opens system settings. Session does not start. |
| Location denied | Scan | "Location is required to place each event." Action opens system settings. Session does not start. |
| Idle | Scan | Preview may show. No overlay. Start session is the primary control. |
| Starting | Scan | Service start. Controls wait. Failure: "The scan did not start." Start is available again. |
| Running | Scan | Overlay, session status, stop control. |
| Preview off | Scan | Preview and overlay hidden. Session status remains. |
| Tractor, screen off | Scan | Dim, or screen off. On screen off, the service notification is "Screen off. Scanning continues." The notification can stop the session and can show the pressure sentence. |
| Weak GPS | Scan, Event detail | Session status says "Weak GPS". Stored events that kept a low-accuracy fix show "Weak GPS" with the accuracy. |
| Pressure | Scan | Pressure alert, and the segment is stored flagged for Session review. |
| Thermal | Scan | Thermal notice. Rate drop is already in effect when the notice shows. A lighter tier applies on the next frame after the user accepts it. |
| Storage cap | Scan, Settings | Storage meter plus "Image storage is full. New events keep their place. New photos wait until you free space." |
| Session ended by the service | Scan | "The scan stopped." Sessions shows the partial session. |
| Empty sessions | Sessions | "No sessions yet." Start is on this surface and on Scan. |
| Session loading | Sessions, Session review | Rows or map area hold their place until the repository returns. |
| No events | Session review | Map or empty map area, and "No events in this session." Export offers an empty file and says the file has no events. |
| No tiles | Session review | "No offline map tiles on this device." List, filters, and export still work. |
| Filter empty | Session review | "No events match." Clear filter. |
| Photo skipped or cleaned up | Event row, Event detail | "No photo." Row, feedback, and navigate stay available. |
| Download or export in progress | Packs, Session review | Determinate progress. The system notification carries the same progress if the user leaves. Failure offers retry. A finished export opens the share sheet. |
| Developer off | Scan, Settings | Debug HUD, video replay, and GPX replay are absent. |
| Developer on | Scan | Debug HUD. Settings can choose video replay and GPX replay as the sources for the next session. A missing replay file does not start the session, and the UI names the missing file. |

Offline is the normal state on every surface except the optional download action.

## Interaction Primitives

- Tap is the farmer's action. Targets are at least `{spacing.touch}`.
- Session start is explicit and uses Start sheet.
- Session stop is explicit on Scan and on the service notification. Stop ends the session and writes the end time.
- System back leaves Event detail, then Session review, then the tab. Back does not stop a running scan. Stop does.
- The share sheet is the only way a file leaves the phone. There is no account and no sync.
- Navigate action hands the canopy point to another app with a geo intent. Bearing and distance stay in Event detail.
- Cleanup of old images is a confirmed Settings action. It deletes image files and keeps the event rows. Events are not swipe-deleted.
- [ASSUMPTION] A Settings row imports offline map tiles from local storage. Review never downloads tiles.
- A running session does not pick up setting edits. The next session does.

## Accessibility Floor

Behavioral. Contrast lives in `DESIGN.md` Colors.

- TalkBack reads every control with its name and state. The pressure alert is announced when it appears, including when it arrives as the service notification.
- Class is never color alone. Overlay, marker content description, and list row include the class name.
- Font scale follows the system. At the largest scale, start, stop, feedback, and export remain on screen without horizontal scrolling.
- Touch targets are at least `{spacing.touch}`.
- Focus order on Scan is Session status, Pressure alert, Tractor control, start or stop, then navigation.
- The screen-off notification is the accessible path for tractor mode. Vibration accompanies the pressure notification. [ASSUMPTION] The spec does not mention vibration. It is included so the alert exists when the screen is off and the farmer is not looking at it.

## Responsive & Platform

Single surface: Android phone. Portrait and landscape layout is in `DESIGN.md` Layout & Spacing. There is no tablet layout, no web app, and no watch. The service notification is the second surface, and only while a session is running.

## Inspiration & Anti-patterns

- **Required:** MapLibre and tiles already on the phone. A geo intent for "Navigate back to this spot". The Android share sheet for export.
- **Rejected by the spec:** Google Maps, Play services, accounts, cloud sync, and a web dashboard. None of those appear as disabled buttons.
- **Rejected by the spec:** hard-coded class lists and hard-coded class colors.

## Key Flows

[ASSUMPTION] The spec names a farmer and, in the tooling section, development work, but no named person. The farmer is the protagonist of the field flows. The developer is the protagonist of the tooling flow.

### Real-time on-device detection (camera, inference, overlay)

1. The farmer opens Scan with a pack already active and camera and location allowed.
2. The farmer confirms Start sheet.
3. The service starts. Session status shows the pack and a live GPS state.
4. The preview shows the canopy. Detection overlay draws boxes for the latest frame.
5. The farmer keeps walking. Preview can be turned off. Overlay hides with it. Status stays.
6. **Climax:** A box and the manifest class name sit on the leaf the farmer is passing, and Session status stays in the running state.

Failure: camera or location denied stops the flow at step 2 and sends the farmer to system settings. Service start failure shows "The scan did not start."

### Tracking and event logging with GPS and one evidence image per event

1. The farmer is in a running session from the previous flow.
2. The service promotes a track that has lasted the pack's minimum frames and score.
3. The event count on Session status increases by one.
4. The farmer opens Sessions, then that session, then the new Event row.
5. **Climax:** Event detail shows one photo, the class, and the canopy point, and the session list has one row for that sighting.

Failure: storage cap still adds the event. Event detail says "No photo". Weak GPS still stores the point and labels it "Weak GPS". A merge into an existing event does not add a second photo or a second row.

### Session storage, review (map and list), export

1. The farmer stops the session.
2. Sessions shows the new Session row.
3. The farmer opens Session review, pans the map, and filters the list.
4. The farmer opens Export sheet and chooses a format, then the zip of images.
5. **Climax:** The share sheet opens with the finished file, and the session is still on the phone after the share.

Failure: no tiles shows "No offline map tiles on this device." and the list still filters. Export failure offers retry and keeps the rows. Empty session says the file has no events before the share sheet opens.

### Density and severity warnings per area

1. The farmer is scanning while moving.
2. The latest segment crosses the configured density threshold.
3. Pressure alert shows "high disease pressure here".
4. The farmer later opens Session review.
5. **Climax:** That segment is flagged on Review map, with density and severity as separate labels, and the events inside it are ordinary Event rows.

Failure: speed under the configured threshold adds no coverage, so a parked phone does not raise the alert. If the screen is off, step 3 arrives as the service notification with the same sentence.

### Model tiers (several model sizes) selected by device capability

1. The farmer finishes Field advisory and imports a pack on Packs, or chooses the optional download and waits for a passed checksum.
2. Benchmark runs on images already on the device.
3. Benchmark panel names the largest tier that held the frame-rate target.
4. The farmer continues to Scan.
5. **Climax:** Session status names that tier's pack, and a later Settings change overrides it for the next session only.

Failure: checksum failure installs nothing. If the smallest tier cannot hold the frame-rate target, the panel says "This phone cannot hold the minimum frame rate on the smallest model." and Scan still opens with that sentence on Session status. Below API 29, heat does not change the tier.

### Desktop-testable development: stand-in models, video replay, GPX replay

1. The developer turns the developer setting on.
2. Settings offers video replay and GPX replay for the next session, and a stand-in pack is selectable on Packs.
3. The developer starts Scan. Debug HUD shows FPS, inference latency, temperature, and open tracks.
4. **Climax:** The overlay and the event list match the replay sources, and replay uses the same service path as the farmer's Scan.

Failure: with the developer setting off, those controls and Debug HUD are absent. A missing replay file does not start the session, and the UI names the missing file.
