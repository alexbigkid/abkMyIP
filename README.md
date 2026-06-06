# abkMyIP

A small Kotlin Multiplatform project that shows your public IP address (the VPN egress IP when a VPN is active), plus city, country, timezone, coordinates, and a static map of your location.

The same business logic powers:

- **iOS + iPadOS** (SwiftUI)
- **macOS** (SwiftUI)
- **Android** (Jetpack Compose)
- **Linux** (Kotlin/Native CLI)
- **Windows** (Kotlin/Native CLI)
- **Web** (Kotlin/JS in the browser)

## Quick start

```bash
# Fast feedback — run shared unit tests on the JVM
./gradlew :shared:jvmTest

# Build a Linux CLI binary
./gradlew :apps:linuxApp:linkReleaseExecutableLinuxX64

# Build a Windows CLI binary (cross-compile)
./gradlew :apps:windowsApp:linkReleaseExecutableMingwX64

# Serve the web app locally
./gradlew :apps:webApp:jsBrowserDevelopmentRun
```

See [`CLAUDE.md`](./CLAUDE.md) for full module layout, environment prerequisites, and architecture notes.
See [`docs/architecture.md`](./docs/architecture.md) for diagrams.
See [`docs/todo/todo_list.md`](./docs/todo/todo_list.md) for what's done and what's next.
