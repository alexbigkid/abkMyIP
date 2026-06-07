# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A Kotlin Multiplatform (KMP) project that looks up the user's public IP, city, country, timezone, and coordinates via [ipinfo.io](https://ipinfo.io) and renders the location as an OpenStreetMap embed URL. The same business logic powers apps on **Android, iOS, iPadOS, macOS, Linux, Windows, and the Web**.

## Repository layout — load-bearing rules

- **Production code lives under `src/` directories** inside each module (`shared/src/`, `apps/<platform>/src/`).
- **Test code lives under the top-level `tests/` directory**, never inside `src/`. The `tests/` tree mirrors the production layout 1:1 (`shared/src/commonMain/kotlin/com/abk/myip/usecase/Foo.kt` ↔ `tests/shared/commonTest/kotlin/com/abk/myip/usecase/FooTest.kt`). This is enforced via `kotlin.srcDir(rootProject.file("tests/..."))` in every module's `build.gradle.kts`. Do **not** create or use the default `src/<target>Test/` directories — production binary builds must not be able to bundle test code.
- **Kotlin test files use the `FooTest.kt` suffix**, not the `test_` prefix. (This overrides the global rule in the user's `~/.claude/CLAUDE.md`; the suffix is mandatory for Kotlin tooling.)
- **No example code under `src/`** — examples go in `docs/`.

## Architecture (Clean Architecture lite)

```
Platform UI (SwiftUI / Jetpack Compose / DOM / println)
        │
        ▼
usecase/  ── GetMyIpInfoUseCase, BuildStaticMapUrlUseCase
        │
        ▼
data/     ── IpInfoRepository ⇢ IpApiService (Ktor)
        │
        ▼
domain/   ── IpInfo, GeoLocation, StaticMapUrl
```

- All of the above is **shared in `shared/src/commonMain/`** — only the Ktor HTTP engine and the `platformName` string are `expect`/`actual`.
- `AbkMyIp` (in `shared/src/commonMain/kotlin/com/abk/myip/AbkMyIp.kt`) is the entry point apps use — it wires repository, service, and use cases.
- VPN handling is implicit: ipinfo.io returns whichever IP the request egressed from, which is the VPN's exit IP when a VPN is active.

## Module map

| Module | Purpose | Test source |
|---|---|---|
| `shared` | KMP library — all business logic, 8 targets (android, jvm, ios×3, macos×2, linux×2, mingwX64, js) | `tests/shared/` |
| `apps/androidApp` | Jetpack Compose Android app | `tests/apps/androidApp/` |
| `apps/iosApp` | SwiftUI source for iOS + iPadOS (Xcode project generated locally — see `apps/iosApp/README.md`) | `tests/apps/iosApp/` |
| `apps/macosApp` | SwiftUI source for macOS (Xcode project generated locally — see `apps/macosApp/README.md`) | `tests/apps/macosApp/` |
| `apps/linuxApp` | Kotlin/Native CLI (linuxX64, linuxArm64) | `tests/apps/linuxApp/` |
| `apps/windowsApp` | Kotlin/Native CLI (mingwX64) | `tests/apps/windowsApp/` |
| `apps/webApp` | Kotlin/JS browser app | `tests/apps/webApp/` |

Linux and Windows ship as **CLI binaries** because Kotlin/Native has no production GUI toolkit for those platforms.

## Common commands

```bash
# Fast feedback loop — run all shared common tests on the JVM (no Xcode / no Android SDK needed)
./gradlew :shared:jvmTest

# All shared unit tests across every host-runnable target
./gradlew :shared:allTests

# Cross-compile shared module for a target (verifies the multiplatform sources without linking)
./gradlew :shared:compileKotlinLinuxX64
./gradlew :shared:compileKotlinMingwX64
./gradlew :shared:compileKotlinJs

# Build & link a Linux native CLI binary
./gradlew :apps:linuxApp:linkReleaseExecutableLinuxX64
# Resulting binary: apps/linuxApp/build/bin/linuxX64/releaseExecutable/linuxApp.kexe

# Build & link a Windows native CLI binary (cross-compiles on macOS/Linux too)
./gradlew :apps:windowsApp:linkReleaseExecutableMingwX64

# Run the web app locally
./gradlew :apps:webApp:jsBrowserDevelopmentRun

# Build the Android app (requires Android SDK + ANDROID_HOME)
./gradlew :apps:androidApp:installDebug

# Build iOS / macOS frameworks for the Xcode projects to consume
./gradlew :shared:linkReleaseFrameworkIosArm64
./gradlew :shared:linkReleaseFrameworkMacosArm64
```

## Environment prerequisites

| Target | Required tools |
|---|---|
| `:shared:jvmTest`, `:apps:webApp`, `:apps:linuxApp`, `:apps:windowsApp` | JDK 17+ only (the Gradle wrapper provides Kotlin/Native and downloads sysroots) |
| `:apps:androidApp` | Android SDK with `ANDROID_HOME` set, `compileSdk = 36` |
| iOS / macOS native + iOS sim tests | **Full Xcode** (not just Command Line Tools) — Kotlin/Native invokes `xcrun xcodebuild` |

### Optional: `IPINFO_TOKEN`

ipinfo.io's free tier rate-limits per egress IP fairly aggressively. To lift the limit, get a token at <https://ipinfo.io> and provide it via either:

- **Environment variable** `IPINFO_TOKEN` (works for terminal builds, `direnv` + `pass`, CI). Preferred.
- **`.env` file** at the repo root with `IPINFO_TOKEN=<value>` (see `.env.example`). This path is the one Xcode hits, since Run-Script build phases do not inherit the shell's env vars.

The token is read at Gradle config time, baked into `BuildConfig.IPINFO_TOKEN` for all 7 targets, and sent as `Authorization: Bearer <token>` from the shared `IpApiService`. Builds succeed without a token (the header is simply omitted, and you live with the rate limit).

**Web caveat**: the token is plaintext-visible in the served JS bundle. Fine for local dev (`./gradlew :apps:webApp:jsBrowserDevelopmentRun`). Do **not** publish the web bundle with a real token baked in — that needs a server-side proxy instead.

## Tech stack

- **Kotlin** 2.1.0, **Gradle** 8.10.2 (wrapper), **AGP** 8.7.3
- **Ktor** 3.0.3 client with per-target engines: OkHttp (Android/JVM), Darwin (Apple), Curl (Linux), WinHttp (Windows), Js (Web)
- **kotlinx.serialization** for JSON
- **kotlinx.coroutines** + `kotlinx-coroutines-test` for async + testing
- **kotlin.test** + Ktor `MockEngine` for tests
- **Jetpack Compose** + Coil for the Android UI
- **SwiftUI** + `AsyncImage` for iOS/iPadOS/macOS
- Version catalog at `gradle/libs.versions.toml` is the single source of truth — never hard-code versions in module build files.

## Development guidelines that apply here

- **TDD** — write tests under `tests/shared/commonTest/` first, then implement.
- **No hard-coded strings** — use constants (e.g., the ipinfo.io endpoint and OSM embed base URL are private consts in their callers).
- **Imports at file level** in tests.
- **Don't mock the logger** — log output may change without invalidating a test.
- Progress for ongoing features tracked in `docs/todo/todo_list.md`.
