# abkMyIP — Progress tracker

## Phase 1: Bootstrap (in progress)

### Shared module
- [x] Gradle root + wrapper (8.10.2)
- [x] Version catalog (`gradle/libs.versions.toml`)
- [x] KMP targets: android, jvm, iosX64/Arm64/SimulatorArm64, macosX64/Arm64, linuxX64/Arm64, mingwX64, js
- [x] Ktor client + per-platform engines wired
- [x] Default hierarchy template applied; test source sets relocated to `tests/shared/`
- [x] Domain: `IpInfo`, `GeoLocation`, `StaticMapUrl`
- [x] Data: `IpInfoDto`, `IpApiService` (Ktor), `DefaultIpInfoRepository`
- [x] Use cases: `GetMyIpInfoUseCase`, `BuildStaticMapUrlUseCase`
- [x] Composition root: `AbkMyIp`
- [x] Common tests (TDD): 21 tests across 6 classes, all green on JVM
- [ ] Common tests green on every native target (blocked locally by missing full Xcode)
- [ ] Kover (`org.jetbrains.kotlinx:kover`) wired in for coverage; target 100% on shared

### Platform apps
- [x] `apps/linuxApp` — CLI scaffold + sanity test
- [x] `apps/windowsApp` — CLI scaffold
- [x] `apps/webApp` — Kotlin/JS + `index.html`
- [x] `apps/androidApp` — Jetpack Compose `MainActivity` + manifest
- [x] `apps/iosApp` — SwiftUI sources + Xcode setup README
- [x] `apps/macosApp` — SwiftUI sources + Xcode setup README
- [ ] Generate / commit Xcode project files for iOS and macOS
- [x] Verify Android build end-to-end on an emulator (Pixel 10 / API 36.1, OSMDroid native map, edge-to-edge UI, pull-to-refresh, retry-on-error)
- [ ] Verify Linux native binary produces correct output against ipapi.co
- [ ] Verify Web app in browser

### Documentation
- [x] `CLAUDE.md` with the load-bearing test-location rule
- [x] `docs/architecture.md` with module + flow + test-layout Mermaid diagrams
- [x] This `docs/todo/todo_list.md`
- [ ] Top-level `README.md` rewrite (still the 2-line stub)

## Phase 2: Hardening (not started)
- [ ] Add Kover for coverage reporting
- [ ] Error handling: typed `Result<IpInfo, IpLookupError>` instead of throwing
- [ ] Retry / timeout configuration on the Ktor client
- [ ] Caching layer with a short TTL so opening the app twice in a row is instant
- [ ] CI pipeline (GitHub Actions matrix: macos-latest for Apple, ubuntu-latest for Linux/JVM/JS, windows-latest for mingw)

## Phase 3: Feature polish (in progress)
- [x] Pull-to-refresh on Android (Material3 `PullToRefreshBox`); iOS still pending
- [ ] Multiple IP services with fallback (ipapi.co → ipinfo.io → ip-api.com)
- [x] Map zoom / pan on Android (OSMDroid native MapView replaces the iframe-only shared `StaticMapUrl`); iOS/macOS already use MapKit natively
