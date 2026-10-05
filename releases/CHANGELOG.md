# Changelog

## [1.4.0] - 2026-10-05

### Added
- Per-world broadcast targeting via `target_worlds` config option for both interval and scheduled messages.
- Per-permission broadcast targeting via `required_permission` config option.
- Interval validation with minimum 10-second safeguard.
- Improved message format detection (MiniMessage vs legacy) using regex-based tag detection.
- Better tab completion: suggests indices for `remove interval` and time keys for `remove time`.
- Added `ValidationUtil` with extracted testable logic.

### Fixed
- Scheduled messages could fire duplicates after reload (added per-day "already fired" tracking).
- Scheduled messages could be missed if ticks are skipped (now uses a 2-minute window instead of exact match).

## [1.3.0] - 2026-06-26

### Added
- Replaced custom analytics with bStats (service ID: 32227) — standard opt-out via plugins/bStats/config.yml (#5)
- Improved error handling: prevent silent failures and task death (#3)
- Security hardening: configurable analytics, input validation, immutable collections (#2)
- Refactored shared utilities to eliminate code duplication (#1)
- Added comprehensive unit test suite (60 tests) covering all modules (#4)

## [1.2.1] - 2026-07-01

### Changed
- Updated Paper API dependency.

## [1.2.0] - 2026-06-20

### Added
- Support for adding and removing messages in-game via `/ab add` and `/ab remove` commands.

## [1.1.0] - 2026-06-15

### Added
- Integrated `plugin-analytics-api` for anonymous usage tracking.
- Added Gradle Shadow plugin to shade the analytics API into the jar.

### Changed
- Updated version to `1.1.0`.
