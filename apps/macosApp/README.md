# macosApp — SwiftUI app for macOS

This directory holds the SwiftUI source for the macOS app. The Xcode project (`.xcodeproj`) is not committed because it is generated locally — see the analogous steps in `../iosApp/README.md`.

## One-time setup

1. **Build the macOS XCFramework**
   ```
   ./gradlew :shared:linkReleaseFrameworkMacosArm64
   ./gradlew :shared:linkReleaseFrameworkMacosX64
   ```

2. **Create the Xcode project**
   - **File → New → Project → macOS → App**
   - Product name: `macosApp`, Interface: SwiftUI, Language: Swift
   - Save under `apps/macosApp/`
   - Replace the generated Swift files with the ones already in `apps/macosApp/macosApp/`

3. **Link the shared framework** and add the same `embedAndSignAppleFrameworkForXcode` Run Script phase as iOS (see `../iosApp/README.md`).

4. **Point XCTest sources at `tests/apps/macosApp/`** so they live outside `src/`.

5. Build and run.

## Note on iOS vs macOS targets

The `shared/build.gradle.kts` already declares `macosX64()` and `macosArm64()` targets and produces frameworks for both. The `ContentView.swift` here is intentionally near-identical to the iOS one — SwiftUI lets the same view code run on both with minor layout tweaks.
