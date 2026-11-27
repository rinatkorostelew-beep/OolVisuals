# OolVisuals performance pass

- Soft Aim now checks target distance and angular suitability before running the line-of-sight test. This avoids unnecessary visibility checks for players outside the configured range/cone.
- Soft Aim caches the selected target yaw/pitch during the scan instead of recalculating trigonometry for the selected player a second time.
- Hit-distance HUD text is formatted once when a hit is recorded, rather than calling `String.format` every rendered frame for the six-second display period.

No feature defaults, configuration keys, ranges, or UI controls were changed.

Build verification was not run in this environment because Gradle and a Gradle wrapper are not present in the supplied project archive.
