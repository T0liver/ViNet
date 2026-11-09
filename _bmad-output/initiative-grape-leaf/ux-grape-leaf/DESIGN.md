---
name: ViNet
description: Field app for vineyard scanning. Vine green and sky blue chrome, a grape-leaf mark, and class color only from the model pack.
status: final
updated: 2026-11-08
colors:
  surface: '#F3F6F0'
  surface-dark: '#0C1410'
  surface-raised: '#FFFFFF'
  surface-raised-dark: '#152019'
  ink: '#0E1A14'
  ink-dark: '#E7F2EA'
  ink-secondary: '#3E5348'
  ink-secondary-dark: '#B7C9BE'
  outline: '#C5D0C6'
  outline-dark: '#2A3830'
  vine: '#085C30'
  vine-dark: '#B6F5C4'
  on-vine: '#FFFFFF'
  on-vine-dark: '#062616'
  sky: '#0057B8'
  sky-dark: '#9FD4FF'
  leaf-deep: '#064E28'
  leaf-mid: '#078F47'
  leaf: '#18A149'
  leaf-light: '#8EF0A8'
  recording: '#8C2F2F'
  recording-dark: '#F0B4AE'
typography:
  wordmark:
    fontFamily: Fraunces
    fontSize: 36px
    fontWeight: '600'
    lineHeight: '40px'
    letterSpacing: '-0.02em'
  title:
    fontFamily: Source Sans 3
    fontSize: 24px
    fontWeight: '400'
    lineHeight: '32px'
    letterSpacing: '0'
  body:
    fontFamily: Source Sans 3
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '24px'
    letterSpacing: '0.5px'
  label:
    fontFamily: Source Sans 3
    fontSize: 14px
    fontWeight: '600'
    lineHeight: '20px'
    letterSpacing: '0.1px'
  meta:
    fontFamily: Source Sans 3
    fontSize: 12px
    fontWeight: '400'
    lineHeight: '16px'
    letterSpacing: '0.4px'
rounded:
  sm: 8px
  md: 12px
  lg: 16px
  full: 9999px
spacing:
  '1': 4px
  '2': 8px
  '3': 12px
  '4': 16px
  '5': 24px
  '6': 32px
  margin: 16px
  touch: 48px
components:
  detection-overlay:
    stroke: 'class color from the active model pack manifest'
    label-background: '{colors.surface-raised}'
    label-foreground: '{colors.ink}'
    radius: '{rounded.sm}'
  session-status:
    background: '{colors.surface-raised}'
    foreground: '{colors.ink}'
    recording: '{colors.recording}'
    radius: '{rounded.md}'
  pressure-alert:
    background: '{colors.surface-raised}'
    foreground: '{colors.ink}'
    border: '{colors.ink}'
    radius: '{rounded.md}'
  debug-hud:
    background: '{colors.surface-dark}'
    foreground: '{colors.ink-dark}'
    radius: '{rounded.sm}'
  session-row:
    background: '{colors.surface}'
    foreground: '{colors.ink}'
    divider: '{colors.outline}'
  review-map:
    marker-stroke: 'class color from the manifest'
    marker-label: '{colors.ink}'
    segment-flag: '{colors.ink}'
  event-row:
    background: '{colors.surface}'
    foreground: '{colors.ink}'
    meta: '{colors.ink-secondary}'
    radius: '{rounded.sm}'
  event-detail:
    background: '{colors.surface}'
    foreground: '{colors.ink}'
  feedback-control:
    background: '{colors.surface-raised}'
    foreground: '{colors.ink}'
    selected-border: '{colors.ink}'
    radius: '{rounded.sm}'
  storage-meter:
    track: '{colors.outline}'
    fill: '{colors.vine}'
    foreground: '{colors.ink-secondary}'
  pack-transfer:
    background: '{colors.surface-raised}'
    foreground: '{colors.ink}'
    radius: '{rounded.md}'
  benchmark-panel:
    background: '{colors.surface-raised}'
    foreground: '{colors.ink}'
    radius: '{rounded.md}'
  thermal-notice:
    background: '{colors.surface-raised}'
    foreground: '{colors.ink}'
    radius: '{rounded.md}'
  field-advisory:
    background: '{colors.surface-raised}'
    foreground: '{colors.ink}'
    radius: '{rounded.md}'
  settings-row:
    background: '{colors.surface}'
    foreground: '{colors.ink}'
    value: '{colors.ink-secondary}'
    divider: '{colors.outline}'
  export-sheet:
    background: '{colors.surface-raised}'
    foreground: '{colors.ink}'
    radius: '{rounded.lg}'
  navigate-action:
    background: '{colors.vine}'
    foreground: '{colors.on-vine}'
    radius: '{rounded.sm}'
  list-filter:
    background: '{colors.surface-raised}'
    foreground: '{colors.ink}'
    selected-border: '{colors.vine}'
    radius: '{rounded.full}'
  tractor-control:
    background: '{colors.surface-raised}'
    foreground: '{colors.ink}'
    radius: '{rounded.sm}'
  start-sheet:
    background: '{colors.surface-raised}'
    foreground: '{colors.ink}'
    radius: '{rounded.lg}'
  leaf-mark:
    leaf-deep: '{colors.leaf-deep}'
    leaf-mid: '{colors.leaf-mid}'
    leaf: '{colors.leaf}'
    leaf-light: '{colors.leaf-light}'
    vein: '{colors.ink}'
    net: '{colors.sky}'
    node: '{colors.on-vine}'
    node-stroke: '{colors.ink}'
  wordmark:
    font: '{typography.wordmark}'
    foreground: '{colors.vine}'
---

## Brand & Style

ViNet (`hu.toliver.vinet`) is a field instrument for one job: walk a vineyard or ride a tractor, keep a trustworthy record of what the camera saw, and look it up afterward. The brand should feel close to the vine, the leaf, and the sky over a field. Chrome is vine green and sky blue on a pale leaf-paper ground. It stays sharp in sun: deep fills, light type, no washed pastel as a control.

The mark is the drawing in `docs/vinet.svg`, colored in `docs/vinet_c.svg`. It is one leaf split down the middle. The left half is a grape leaf: serrated edge, stem, and veins. The right half is the same outline filled by a net of round nodes. That split is the identity. Do not redraw it, do not mirror the leaf onto the right, and do not drop the nodes.

Dynamic color is off, so a wallpaper hue cannot sit next to a class color and look like a second disease. Class color is data. It is read from the model pack manifest at runtime and is never a theme token.

The product voice is plain and specific. It names the class, the place, and the limit. It does not encourage, score, or celebrate a scan.

## Colors

The brand is two hues. Vine is the leaf and the primary action. Sky is water and open air, used in small amounts. Everything else is green-black ink on a pale ground so the field stays readable.

- **Vine (`{colors.vine}` / `{colors.vine-dark}`)** is the primary control fill: start, navigate, the selected filter, and the storage fill. Light mode puts `{colors.on-vine}` on it at about 8:1. Dark mode fills with `{colors.vine-dark}` and writes `{colors.on-vine-dark}` on it at about 13:1. The launcher is not a vine field. The mark sits on `{colors.surface-raised}` so the leaf and the net both stay visible.
- **Leaf ramp.** The colored file paints the leaf in four greens that sit almost on top of each other (`#078F47`, `#118441`, `#18A147`, `#18A149`). The spine keeps that four-plane structure and spreads it. `{colors.leaf-deep}` is the darkest lobe, `{colors.leaf-mid}` is the file's `#078F47`, `{colors.leaf}` is the file's `#18A149`, and `{colors.leaf-light}` is the new bright plane. Black-green veins stay `{colors.ink}`. These four are for the mark only, not for buttons.
- **Sky (`{colors.sky}` / `{colors.sky-dark}`)** is the net behind the nodes, and the active navigation mark. The file's net blue is `#008BFF`. White nodes on that blue are about 3.4:1, so the spine uses `{colors.sky}`, the same blue pushed darker, where white is about 6.9:1. `{colors.sky-dark}` is the sky accent on a dark surface. Sky is never a disease and never the recording state.
- **Surface (`{colors.surface}` / `{colors.surface-dark}`)** is the screen behind lists, settings, and review. **Surface raised (`{colors.surface-raised}` / `{colors.surface-raised-dark}`)** is the status chip, alert, sheet, and overlay label.
- **Ink (`{colors.ink}` / `{colors.ink-dark}`)** is primary text. **Ink secondary (`{colors.ink-secondary}` / `{colors.ink-secondary-dark}`)** is metadata: time, accuracy, pack version. Ink on surface is about 16:1. Secondary on surface is about 7.6:1. The dark theme matches those ratios. Target is at least 4.5:1 for `{typography.meta}` and 7:1 for `{typography.body}`.
- **Outline (`{colors.outline}` / `{colors.outline-dark}`)** is dividers and the storage track. It is not used for text.
- **Recording (`{colors.recording}` / `{colors.recording-dark}`)** is only the live-session dot. It stays red-brown so a live session is not the same hue as the vine.
- **Class color** has no hex in this file. Each class brings `color` from the manifest. The overlay stroke, map marker, and list swatch use that hex. Label text does not: it uses `{colors.ink}` on `{colors.surface-raised}`, because a manifest color may fail contrast and may sit near vine green.

The pressure alert does not use vine or sky. Weight comes from the sentence, a 2px `{colors.ink}` border, and placement.

Dark tokens follow the system theme. Component tokens name the light role. In the dark theme, swap chrome roles for the `-dark` partner (`surface`, `surface-raised`, `ink`, `ink-secondary`, `outline`, `vine`, `on-vine`, `sky`, `recording`). The leaf mark does not swap. It uses the same leaf ramp, sky net, and white nodes in both themes. Tractor mode is screen dim or screen off, not a third palette.

## Typography

The wordmark is Fraunces, `{typography.wordmark}`. It is used for the name ViNet next to the leaf, on the launcher and on Field advisory. Fraunces has a soft, plant-like stress that sits with a leaf silhouette. It is not used on controls, overlays, or the map.

UI type is Source Sans 3, bundled with Fraunces. `{typography.title}` matches Headline Small, `{typography.body}` matches Body Large, `{typography.label}` matches Label Large, `{typography.meta}` matches Body Small. Sizes above are the default font scale. The system font scale changes the UI roles. The wordmark stays at `{typography.wordmark.fontSize}`. Layout keeps `{spacing.touch}` targets at the largest scale.

Use `{typography.title}` for the surface name and the pressure sentence. Use `{typography.body}` for class names and settings. Use `{typography.label}` on controls. Use `{typography.meta}` for GPS accuracy, time, model version, and counts. No all-caps labels.

## Layout & Spacing

The scale is `{spacing.1}` through `{spacing.6}`. On Android those dimensions are dp. Screen margin is `{spacing.margin}`. Minimum hit target is `{spacing.touch}`.

One column. The scan preview is edge to edge. Session status, the pressure alert, and Tractor control sit on top of the preview, inset by `{spacing.4}`, and stack from the top. Lists and settings use `{spacing.margin}` and hairline dividers. Content clears the bottom navigation. Sheets cover one level and leave the navigation bar in place.

In portrait, Session review puts Review map on the top half and the list on the bottom half. [ASSUMPTION] In landscape, Scan keeps this stack, and Session review places Review map and the list side by side, because a mount may fix the phone sideways. The list is the full record when the map has no tiles.

## Elevation & Depth

Separate surfaces by tone: `{colors.surface}` under `{colors.surface-raised}`. Shadows are the Material 3 overlay shadow on the export sheet and on the session-start sheet only. Map markers, boxes, and list rows do not float. The preview has no card around it.

## Shapes

`{rounded.sm}` for overlay labels, event thumbnails, and the feedback control. `{rounded.md}` for status, alerts, and panels. `{rounded.lg}` for the export sheet. `{rounded.full}` for the recording dot and the list filter. Class swatches are a 12dp square with `{rounded.sm}`, filled with the manifest color, with `{colors.outline}` behind them so a white or yellow class still has an edge.

## Components

Visual specs. Behavior is in `EXPERIENCE.md`.

- **Detection overlay** — Stroke 2dp in the manifest color for that class, in normalized box space after letterbox removal. Label on `{colors.surface-raised}` in `{colors.ink}`: class name and score. Corner `{rounded.sm}`.
- **Session status** — `{colors.surface-raised}` chip, `{typography.meta}`, recording dot `{colors.recording}` at 12dp while a session is running. Radius `{rounded.md}`.
- **Pressure alert** — Full-width inset banner, `{colors.surface-raised}`, 2px `{colors.ink}` border, `{typography.title}` for the sentence. No fill in a class color.
- **Debug HUD** — `{colors.surface-dark}` at the lower start of the preview, `{typography.meta}` in `{colors.ink-dark}`. Four lines: FPS, inference latency, temperature, open tracks.
- **Session row** — Full-width row, `{typography.body}` for the session plot label or start time, `{typography.meta}` for event count and pack version. Divider `{colors.outline}`.
- **Review map** — MapLibre. Marker fill is the manifest color. A flagged segment is an ink outline along that part of the path, plus the word "density" on the segment label. Severity is a separate `{typography.meta}` value on that label.
- **Event row** — 56dp thumbnail, class swatch, class name, time, plot name. "Weak GPS" is set in `{colors.ink-secondary}`. Radius `{rounded.sm}` on the thumbnail.
- **Event detail** — Full frame on `{colors.surface}`, letterboxed, with the stored box drawn in the class color. Missing file shows `{typography.body}` on `{colors.surface}` where the frame would be.
- **Feedback control** — Three equal segments, `{spacing.touch}` tall, `{rounded.sm}`. Selected segment has a 2px `{colors.ink}` border. Unselected is `{colors.surface-raised}`.
- **Storage meter** — 4dp track in `{colors.outline}`, fill in `{colors.vine}`, caption in `{colors.ink-secondary}`.
- **Pack transfer** — Panel on `{colors.surface-raised}` with name, size, and a determinate bar using the storage-meter track and fill. Checksum failure uses `{typography.body}` in `{colors.ink}`, no extra color.
- **Benchmark panel** — Same surface as Pack transfer. Result is a short sentence in `{typography.body}`, then the chosen tier in `{typography.title}`.
- **Thermal notice** — Same surface and border treatment as Pressure alert, set in `{typography.body}`.
- **Field advisory** — Stacked sentences on `{colors.surface-raised}`, `{typography.body}`, radius `{rounded.md}`.
- **Settings row** — Label `{typography.body}` start, value `{typography.meta}` end, 56dp min height, divider `{colors.outline}`.
- **Export sheet** — `{colors.surface-raised}`, top radius `{rounded.lg}`, one row per export kind, `{spacing.touch}` each.
- **Navigate action** — Filled with `{colors.vine}`, text in `{colors.on-vine}`, `{typography.label}`, height `{spacing.touch}`, radius `{rounded.sm}`.
- **List filter** — Text chips, `{rounded.full}`, selected state is a 2px `{colors.vine}` border. Chip label is the class name, a plot name, or a feedback value.
- **Tractor control** — `{typography.label}` on `{colors.surface-raised}`, height `{spacing.touch}`, radius `{rounded.sm}`. Sits under Session status.
- **Start sheet** — `{colors.surface-raised}`, top radius `{rounded.lg}`, `{typography.body}` rows, primary action uses the Navigate action fill and type.
- **Leaf mark** — Artwork from `docs/vinet.svg`. Left half uses the four leaf fills, veins in `{colors.ink}`. Right half is a `{colors.sky}` field with nodes in `{colors.on-vine}` and a `{colors.ink}` stroke. Used at 108dp on the launcher and 48dp on Field advisory, on `{colors.surface-raised}`. Both halves stay in frame. No extra circle, no redraw.
- **Wordmark** — "ViNet" in `{typography.wordmark}`, color `{colors.vine}` on `{colors.surface}`. It sits beside the mark, not inside either half.

## Do's and Don'ts

| Do | Don't |
|---|---|
| Take class color, name, and score threshold from the manifest | Hard-code disease colors or class names into the theme |
| Put overlay and marker text on `{colors.surface-raised}` | Draw class-name text in the class color |
| Keep both halves of the mark: leaf on the left, node net on the right | Replace the net with a mirrored leaf, or crop the launcher to one half |
| Use `{colors.vine}` for primary actions, and `{colors.sky}` for the net and the active navigation mark | Paint a detection, a warning, or the recording dot in vine or sky |
| Use `{colors.recording}` only for the live-session dot | Use the recording hue for a disease or a healthy row |
| Show density and severity as two labels on a segment | Recolor the map by box area |
| Keep scan controls on a solid chip over the preview | Put critical text directly on the camera image |
| Use MapLibre with tiles already on the device | Use Google Maps or download tiles during a review |
| Follow the system light and dark tokens in this file | Invent a tractor-mode palette |
