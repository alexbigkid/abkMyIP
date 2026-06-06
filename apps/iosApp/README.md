# iosApp — SwiftUI app for iOS & iPadOS

This directory holds the SwiftUI source for the iOS app. The Xcode project (`.xcodeproj`) is not committed because it is generated locally — the recommended bootstrap flow:

## One-time setup

1. **Build the iOS XCFramework**
   ```
   ./gradlew :shared:linkPodReleaseFrameworkIosArm64
   ./gradlew :shared:linkPodReleaseFrameworkIosSimulatorArm64
   ./gradlew :shared:linkPodReleaseFrameworkIosX64
   ```
   These produce `shared.framework` artifacts under `shared/build/bin/`.

2. **Create the Xcode project**
   - In Xcode: **File → New → Project → iOS → App**
   - Product name: `iosApp`, Interface: SwiftUI, Language: Swift
   - Save the project at `apps/iosApp/` (so `iosApp.xcodeproj` sits next to this README)
   - Delete the auto-generated `ContentView.swift` and `iosAppApp.swift` — replace by referencing the ones already in `apps/iosApp/iosApp/` (drag from Finder into the project, **Copy if needed** = unchecked)

3. **Link the shared framework**
   - In the Xcode project target, **General → Frameworks, Libraries, and Embedded Content**: add `shared.framework` from `shared/build/bin/iosSimulatorArm64/podReleaseFramework/` (and add Framework Search Paths for the other variants under `Build Settings` if you want device builds)
   - **Build Phases → New Run Script Phase** (above "Compile Sources"):
     ```
     cd "$SRCROOT/../.."
     ./gradlew :shared:embedAndSignAppleFrameworkForXcode
     ```
   - **Build Settings → User-Defined**: set `KOTLIN_FRAMEWORK_BUILD_TYPE = Debug` (or `Release`)

4. **Set test sources outside src/**
   - When adding the XCTest target, choose **Project Editor → Target → Build Phases → Compile Sources** and reference `tests/apps/iosApp/` instead of the default `iosAppTests/` directory.

5. Build and run — the app should display your IP, city, timezone, and a static OpenStreetMap image.

## iPadOS

The Xcode iOS target covers iPadOS automatically — no separate project needed. iPad layout falls out of SwiftUI's adaptive layout.
