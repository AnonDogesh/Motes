# Motes

Motes is an Android note-taking app built with Kotlin, Jetpack Compose, Navigation Compose, and Room.

## Features

- Unified home feed for three content types:
  - text notes
  - checklists
  - drawings
- Per-type editor screens with autosave
- Archive management for all content types
- Multi-select actions and basic filtering

## Project structure

- `app/src/main/java/com/example/motes/ui`: Compose screens, shared components, and ViewModels
- `app/src/main/java/com/example/motes/data`: Room entities/DAOs, repositories, and app container wiring
- `app/src/main/java/com/example/motes/navigation`: route definitions and navigation host

## Build requirements

- Android Studio with Android SDK configured
- JDK 17 or JDK 21 recommended
- Gradle installation available on your machine (the Gradle wrapper is not currently committed)

## Build and run

From the repository root:

```bash
gradle :app:assembleDebug
```

To install to a connected device/emulator:

```bash
gradle :app:installDebug
```

## Validation commands

```bash
gradle :app:lint
```

```bash
gradle test
```

> Note: in this environment, `gradle test` may fail when run with Java 25 due to Kotlin script parsing incompatibility. Use JDK 17/21 for local validation.

## Known gaps

- No Gradle wrapper (`./gradlew`) is currently committed.
- Automated unit/instrumentation test suites are not yet present in the repository.
