# Adversary review — Grape leaf architecture spine

Verdict: **fail**

Stance: two units one level down (modules / epics) each obey every adopted AD and still cannot be linked. Style, naming taste, and deferred choices that a single later decision can still close are not findings. Altitude is initiative, so the units are the epics that would build `:camera`, `:inference`, `:tracking`, `:location`, `:data`, `:density`, `:export`, and `:app`.

---

## 1. Critical — Best-frame bytes have no legal custodian

**Pair**

- `:camera` — `FrameSource` emits a frame that is valid only until the CameraX analyzer returns. The analyzer keeps no queue. That is AD-4.
- `:tracking` — returns a best-frame score and the frame timestamp. It does not retain pixels. JVM module, no Android image type. That is AD-2 and AD-5 ("return values").
- `:data` — writes `imagePath` and `thumbPath` once, when the track ends, and is the only writer of image files, so it also has to be the EXIF writer (AD-5, AD-7). At track end it is handed nothing it can encode.

**Clash:** the sentence "best-frame bytes stay in memory" names no type, no module, and no moment. The only moment a copy is still valid is inside the analyzer callback, before the latest-frame policy releases the buffer. Camera is forbidden to keep the previous frame. Tracking is forbidden to own the buffer. Data writes too late. Each epic is compliant; the evidence file is already gone.

The same gap splits the file contract:

- Data writes a downscaled full frame as `thumbPath`. Review draws a crop of the detection. Both store one thumb path (AD-6).
- Data stores the track's last box. Tracking selects the image from the best frame. Review paints that box on that image. AD-3 puts both in the same 0..1 space and never says they are the same sample.
- Data stores an absolute `filesDir` path. Export uses `imagePath` as the zip entry name (AD-5 read, AD-9 share). The GeoJSON inside the zip does not point at the zip entry.

**Close:** one AD that names the foreground service as the only best-frame custodian: synchronous immutable copy before the analyzer returns; encoding (full-frame bytes, thumb derivation, EXIF applied by `:data` at the single write); the stored box is the box of that same frame; `imagePath` / `thumbPath` are repository-relative.

---

## 2. High — An empty detection list is either a miss or a gap

**Pair**

- `:app` pipeline — calls `Detector.detect` synchronously, then calls the tracker only when the list is non-empty. Frames the camera dropped are gaps. The tracker is not asked to invent boxes. That is AD-4.
- `:tracking` — a delivered empty list is a miss that ages the track toward its end; a hole in `frameTimestampNs` is a gap and does not count as a miss. It never synthesizes a box for the hole. That is also AD-4.

**Clash:** AD-4 defines a gap as a dropped frame and does not require every analyzed frame, including a zero-detection result, to be delivered. The pipeline epic treats "no call" as the gap. The tracker epic treats "no call" as a missed miss. Tracks either never close or close on real camera drops. `minTrackFrames` then fires on different lifetimes, so the two epics do not create the same events.

**Close:** every frame that `detect` returned, including an empty list, is a tracker input. Only a frame that never reached `detect` is a gap. State the miss rule (empty list increments age; a gap does not).

---

## 3. High — Same 0..1 box space, two geometries

**Pair**

- `:inference` — `NormalizedBox` is `x1,y1,x2,y2`, origin top-left, y down, in the upright buffer after it rotates the sensor image and then removes letterbox. `Detection` carries `classId: Int`.
- `:app` overlay and `:tracking` — `NormalizedBox` is `cx,cy,w,h` on the sensor-oriented buffer the camera calls "the source frame", and association uses the class name from the manifest. Still 0..1 after letterbox removal. That is AD-3.

**Clash:** AD-3 forbids a second coordinate space and does not define axis order, origin, y direction, xyxy versus center-size, or whether "source frame" is before or after the rotation CameraX reports. Both epics stay inside the one space and draw different rectangles. IoU in the tracker does not match the overlay. Parity fails while both sides claim the manifest's letterbox flag was honored.

Class identity is the same kind of hole: the manifest has both `id` and `name` (AD-3), and neither `Detection` nor `Event` is told which one is the join key. Merge "same class" (AD-6) can mean id in tracking and name in data.

**Close:** fix `NormalizedBox` as one representation (field order, origin, y-down) on one buffer orientation (who applies `rotationDegrees`, before letterbox). Fix the class key as the manifest id, with the name looked up from that id.

---

## 4. High — Two writers decide the merge

**Pair**

- `:tracking` — pure function `merge(candidate, eventsInHand) -> create | mergeInto`. Returns the decision. Does not touch Room. Session-scoped list, because that is all the pipeline passed in. That is AD-2, AD-5, and AD-6.
- `:data` — `insertEvent` is the only writer, so it runs the distance/time predicate in SQL against every stored row of that class and folds into the nearest row, keeping that row's image. Also AD-5 and AD-6. The structural seed places merge in `:tracking`; no AD says `:data` must not also apply the predicate.

**Clash:** tracking says create, data merges into yesterday's event, or the reverse. `trackId` stays "the id of the track that created the event" in both readings, so AD-6's trackId sentence survives either winner. Scope (this session versus any stored event versus the same `plotName`) is not deferred with the numeric X and Y; those numbers are config, the set they search is not.

**Close:** one owner. Tracking returns the decision; data applies it unchanged. Name the candidate set (this session's events).

---

## 5. High — Density results and the path they were computed on are not an entity

**Pair**

- `:density` — returns segment values from the live fix stream and the event stream. Adds no meters while speed is under the threshold. Does not write. That is AD-5 and AD-8.
- `:data` — writes sessions, events, and image files only. A `DensitySegment` table is outside that list, and the consistency rule says persisted mutation goes through `:data` only, so `:app` does not invent the table either.
- `:app` review — after the process dies, reads repositories (AD-5) and must flag segments (AD-8: "review-map flags read those segment results", and the UI does not recompute from frames). The only legal recomputation is from stored events. Events have no samples for the stretches where the tractor stopped, so that recomputation adds coverage AD-8 told the live engine to skip.

**Clash:** three compliant designs: ephemeral segments, a table nobody is allowed to own, or a review-time recount that contradicts the live numbers. The ER diagram shows `DensitySegment` and no AD names its writer, its key, or the path (ordered canopy points versus integrated phone speed) whose length is "meter traveled". Export of a GPX *track* has the same missing entity: only event points are stored, so a track and a waypoint file are both valid "KML/GPX" and they are not the same geometry.

**Close:** either persist segments and the distance basis through `:data` (one writer, defined path: which point series, how stopped time is excluded), and say review reads those rows, or state that review recomputes with a named pure function over a stored fix track. Pick one GPX shape (event waypoints, session track, or both) from that same path.

---

## 6. High — The fix flow may drop the samples the join needs

**Pair**

- `:location` — `LocationSource.fixes` is a `Flow` that conflates to the latest fix, mirroring the camera policy. AD-4 requires keep-only-latest for camera analysis and only requires that fixes be a Flow.
- `:location` join — one pure function interpolates along `elapsedRealtimeNs` and, with a single surviving fix, cannot bracket the frame. It does not stamp "the latest fix" onto the event; it returns no position. That is AD-13.
- `:data` — stored `lat`/`lon` is the canopy point (AD-7), so an event row has a position. The pipeline drops any detection the join could not bracket. A second pipeline stores the event and fills position from the only fix it still holds, and calls that interpolation of a one-point window rather than a latest-fix attach.

**Clash:** camera frame loss is specified; fix retention is not. Conflating fixes still satisfies the letter of AD-4. The two pipelines then disagree on whether an unbracketed frame becomes an event, and on the canopy point when intermediate fixes were discarded. Speed samples the density epic needs (finding 5) are the same stream.

**Close:** `fixes` retains every fix until the join has consumed it (conflate is illegal on this flow). Unbracketed frames do not become events. State that explicitly.

---

## 7. High — The event's UTC instant has two clocks

**Pair**

- `:data` — on insert, sets the event instant to `System.currentTimeMillis()`. Stored instants are UTC epoch milliseconds; exporters render ISO-8601 UTC. That is AD-12. This is not a join on UTC, so AD-13's ban is untouched.
- `:location` — the join function returns the interpolated UTC instant as "the event's UTC instant" (AD-13). Session start/end stay wall-clock.

**Clash:** AD-12 binds data to a wall clock and then says the join *key* is AD-13, which a data epic reads as "do not use elapsed realtime as the stored human time" rather than "the stored human time is the interpolated value". AD-13 binds data as well, but only forbids joining on UTC and attaching the latest fix. Both instants can be produced. Export copies whichever column got written. Two field sessions of the same footage do not share timestamps.

**Close:** the stored event instant is the join's interpolated UTC value and nothing else. Session start and end remain wall clock at the service.

---

## 8. High — Pack bytes, manifest document, and decoder site are three contracts

**Pair**

- `:inference` — parses `manifest.json`, nested input block, `backend` value `litert`, and treats `InferenceBackend.run` as returning decoded `List<Detection>` after the one preprocess. SHA-256 is the hash of the weight file. `outputFormat=yolo26-end2end` selects an in-module decoder with one tensor layout.
- `:app` download / import — verifies SHA-256 of the downloaded zip before unpack, then reads a flat `pack.json` whose `backend` is `LiteRT`. It calls `InferenceBackend` as the only inference entry (AD-10) and decodes tensors in `:app`, choosing the decoder from `manifest.outputFormat` and not assuming that one format for every pack (AD-3). Preprocess still lives in `:inference`.

**Clash:** every AD cited is satisfied. The on-disk document, the hashed bytes, the backend vocabulary, and the legal `outputFormat` layouts are not in the deferred list. The deferred host line fixes index fields (packs, versions, URLs, SHA-256, sizes) and a configured URL; it does not fix the pack document or which bytes the hash covers. Two decoders for the same format string break parity while both refuse to hard-code a single format.

**Close:** one manifest document (name, schema, backend tokens, closed `outputFormat` → layout). SHA-256 covers named bytes (the zip, or the weight file, not whichever the epic preferred). The decoder sits next to the preprocess inside `:inference`; `InferenceBackend` / `Detector` returns `List<Detection>`.

---

## 9. Medium — Plot boundaries have no owner

**Pair**

- `:location` — point-in-polygon is a pure function of a polygon list. `event.plotName` is set only there (AD-7). Overlapping polygons: first match.
- `:app` import — user imports a boundary file. Persisted mutation goes through `:data` (consistency rule), but AD-5's write set is sessions, events, and image files, so `:data` has no plot type. Import therefore writes a GeoJSON file of `Feature`s. A second epic writes a Room table of rings. Both call the location function.

**Clash:** `session.plotName` is specified as a label and is not copied onto events. The polygon set that produces `event.plotName` is not an entity, not a file format, and not a tie-break. Two imports build incompatible stores and different names on the same point.

**Close:** polygons are a `:data` document (one format, one name property). The pure function consumes that document. State the overlap rule.

---

## 10. Medium — Feedback is a write with no value set

**Pair**

- `:app` review — updates the event row in place (AD-5) with a free string `fp`.
- `:export` — reads the row and emits a `userFeedback` enum of `correct | wrong | unsure`, and will not export unknown strings.

**Clash:** AD-5 says the update hits the existing row and does not say the column, the vocabulary, or whether class and confidence stay put. Both epics update or read "the feedback" and the files do not round-trip.

**Close:** one field, closed values, and a rule that feedback does not change class, score, box, image, or identity.

---

## 11. Medium — Cleanup and export mutate one file tree with no snapshot rule

**Pair**

- `:data` — cleanup deletes image files and keeps the rows. AD-6 allows that outcome. Data is the only image-file writer, so it is the deleter.
- `:export` — a `androidx.work` job reads those paths and zips them, not on the capture service (AD-9). It does not write rows.

**Clash:** nothing orders the two. An export started before cleanup can zip a path the deleter removes mid-copy, or the other way around. Both obey the letter. There is no generation, snapshot, or "cleanup is exclusive of export" rule.

**Close:** cleanup and export take a repository lock, or export snapshots path + bytes at start, and a missing file after cleanup is the AD-6 absent-file case rather than a torn zip.

---

## 12. Medium — Load-bearing config keys are not named

**Pair**

- `:tracking` — reads `dedupRadiusMeters` and `dedupWindowMs` off the one immutable snapshot (AD-11).
- `:density` — reads `segmentLengthM` and `minSpeedMps` off a sibling object the app also calls "the config snapshot".

**Clash:** the deferral of numeric thresholds, weights, and segment length fences the *values* and, with AD-11, fences a second preferences store. It does not fence the keys. Both epics are on the one object and still disagree on the field that is the merge distance or the speed cutoff. This is a hole in the deferral boundary, not a request to pin the tuned numbers.

**Close:** name the fields. Leave the numbers deferred.

---

## Checked, not holes

- Inference backend winner, index host, which tiers are listed, benchmark media, tile archive format, in-app navigation library, omitting `:devtools` from release, quantization and per-class threshold numbers, later segmentation and RTK. Each deferral names a single place the choice lands (benchmark, index fields, config numbers, the one local tile reader, `:app`, the pack, `Detector` / `LocationSource`). Two epics cannot both invent a conflicting store without breaking the fence.
- Play-services ban, port directions, `minSdk` 26, sideload, no account/sync, elapsed-realtime join key versus UTC storage format, opaque ids minted only by `:data`, one pipeline in the foreground service, and "no images table" are tight enough that a second epic cannot obey them and contradict them.
- Smoothing-versus-offset order stays inside `:location`'s pure functions. One unit owns it, so it is not a cross-epic split yet. It becomes one if a second epic is allowed to reapply the offset.
