# abkMyIP Justfile
# Cross-platform task runner for the Kotlin Multiplatform project.
# Wraps Gradle tasks + simulator/emulator helpers in short, memorable names.

# -----------------------------------------------------------------------------
# Shell config (Just 1.0+ syntax)
set windows-shell := ["powershell.exe", "-c"]
set shell := ["sh", "-c"]

# -----------------------------------------------------------------------------
# Default recipe — list every available recipe.
default:
    @just --list

# -----------------------------------------------------------------------------
# Grouped command reference (matches `just --list` but organized by purpose).
help:
    @echo "abkMyIP - Available Commands:"
    @echo ""
    @echo "Build (compile + link executables/frameworks):"
    @echo "  build                           Assemble shared module (host-runnable targets)"
    @echo "  build-android                   Build Android debug APK"
    @echo "  build-android-release           Build Android release APK (needs signing config)"
    @echo "  build-linux                     Link Linux native binary (linuxX64)"
    @echo "  build-windows                   Link Windows native binary (mingwX64)"
    @echo "  build-web                       Build web app production bundle"
    @echo "  build-ios                       Build iOS shared framework (device + simulator arm64)"
    @echo "  build-macos                     Build macOS shared framework (arm64)"
    @echo "  build-macos-app                 Build the full macOS .app via xcodebuild"
    @echo "  build-ios-app                   Build the full iOS .app for the simulator via xcodebuild"
    @echo "  build-all                       Build shared + Linux + web + Android (host-buildable set)"
    @echo ""
    @echo "Compile-only checks (no linking — fast feedback):"
    @echo "  compile-android                 Verify shared sources compile for Android"
    @echo "  compile-linux                   Verify shared sources compile for Linux"
    @echo "  compile-windows                 Verify shared sources compile for Windows"
    @echo "  compile-js                      Verify shared sources compile for JS"
    @echo "  compile-ios                     Verify shared sources compile for iOS arm64"
    @echo "  compile-macos                   Verify shared sources compile for macOS arm64"
    @echo "  compile-all                     Compile shared for every target"
    @echo ""
    @echo "Run apps:"
    @echo "  run-linux [args]                Run the Linux CLI (builds first)"
    @echo "  run-windows [args]              Run the Windows CLI (build always; exec only on Windows hosts)"
    @echo "  run-web                         Run web dev server with auto-reload"
    @echo "  run-android                     Install + launch Android app on device/emulator"
    @echo "  run-macos                       Build + launch the macOS app"
    @echo "  run-ios                         Build + launch the iOS app on iPhone 17 Pro simulator"
    @echo "  run-ipad-pro                    Build + launch the iOS app on iPad Pro 11-inch (M5) simulator"
    @echo "  run-ipad-mini                   Build + launch the iOS app on iPad mini (A17 Pro) simulator"
    @echo "  open-macos                      Open the macOS Xcode project"
    @echo ""
    @echo "Tests:"
    @echo "  test                            Default: fast JVM tests on shared (~1s)"
    @echo "  test-all                        All host-runnable shared tests"
    @echo "  test-jvm                        Shared JVM tests"
    @echo "  test-android                    Shared Android unit tests"
    @echo "  test-ios                        Shared iOS tests (Apple host only)"
    @echo "  test-macos                      Shared macOS tests (Apple host only)"
    @echo "  test-linux                      Shared Linux tests"
    @echo "  test-windows                    Shared Windows tests"
    @echo "  test-js                         Shared JS tests"
    @echo "  test-linux-app                  Linux CLI sanity tests"
    @echo ""
    @echo "Simulators / Emulators:"
    @echo "  ios-simulator                   Open the iOS Simulator app (Apple host only)"
    @echo "  ios-sim-list                    List installed iOS simulators"
    @echo "  ios-sim-boot device             Boot a named iOS simulator (e.g. 'iPhone 15 Pro')"
    @echo "  ios-sim-shutdown                Shut down all booted iOS simulators"
    @echo "  android-emulator                Start first available Android AVD in background"
    @echo "  android-avds                    List Android AVDs"
    @echo "  android-devices                 List connected Android devices/emulators"
    @echo ""
    @echo "Project tools:"
    @echo "  version                         Print current app version from libs.versions.toml"
    @echo "  deps                            Show shared module dependency tree"
    @echo "  deps-insight dep                Show what pulls in a specific dependency"
    @echo "  clean                           Delete all build outputs"
    @echo "  refresh                         Re-resolve dependencies from remote (ignore cache)"
    @echo ""
    @echo "Code style:"
    @echo "  format                          Auto-format Kotlin (requires ktlint)"
    @echo "  lint                            Lint Kotlin (requires ktlint)"
    @echo ""
    @echo "Usage: just <recipe> [args...]"

# =============================================================================
# Build — compile + link
# =============================================================================

# -----------------------------------------------------------------------------
# Assembles the shared module for host-runnable targets.
build:
    ./gradlew :shared:assemble

# -----------------------------------------------------------------------------
# Builds the Android debug APK.
build-android:
    ./gradlew :apps:androidApp:assembleDebug
    @echo "✅ APK: apps/androidApp/build/outputs/apk/debug/androidApp-debug.apk"

# -----------------------------------------------------------------------------
# Builds the Android release APK (requires signing config).
build-android-release:
    ./gradlew :apps:androidApp:assembleRelease
    @echo "✅ APK: apps/androidApp/build/outputs/apk/release/"

# -----------------------------------------------------------------------------
# Builds the Linux CLI. Native build on Linux hosts, Docker build on macOS/Windows.
build-linux:
    #!/usr/bin/env sh
    set -e
    OS=$(uname -s)
    if [ "$OS" = "Linux" ]; then
        case "$(uname -m)" in
            x86_64)        TASK=LinuxX64;   DIR=linuxX64   ;;
            aarch64|arm64) TASK=LinuxArm64; DIR=linuxArm64 ;;
            *) echo "❌ Unsupported Linux arch: $(uname -m)"; exit 1 ;;
        esac
        ./gradlew ":apps:linuxApp:linkReleaseExecutable${TASK}"
        echo "✅ Linux binary: apps/linuxApp/build/bin/${DIR}/releaseExecutable/linuxApp.kexe"
    else
        if ! command -v docker >/dev/null 2>&1; then
            echo "❌ Docker not found. Install Docker to build the Linux binary on $OS."
            exit 1
        fi
        echo "🐳 Building Linux binary in Docker (host: $OS, target: linux/amd64) — may take several minutes on first run"
        docker build --platform=linux/amd64 -f apps/linuxApp/Dockerfile -t abkmyip-linux:latest .
        echo "✅ Docker image: abkmyip-linux:latest"
    fi

# -----------------------------------------------------------------------------
# Links the Windows native CLI binary (mingwX64, cross-compiles on macOS/Linux).
build-windows:
    ./gradlew :apps:windowsApp:linkReleaseExecutableMingwX64
    @echo "✅ Windows binary: apps/windowsApp/build/bin/mingwX64/releaseExecutable/windowsApp.exe"

# -----------------------------------------------------------------------------
# Builds the web app production bundle.
build-web:
    ./gradlew :apps:webApp:jsBrowserDistribution
    @echo "✅ Web bundle: apps/webApp/build/dist/js/productionExecutable/"

# -----------------------------------------------------------------------------
# Builds the iOS framework (device + simulator arm64). Requires Xcode.
build-ios:
    ./gradlew :shared:linkReleaseFrameworkIosArm64 :shared:linkReleaseFrameworkIosSimulatorArm64
    @echo "✅ iOS frameworks under shared/build/bin/ios*/releaseFramework/"

# -----------------------------------------------------------------------------
# Builds the macOS shared framework (arm64). Requires Xcode.
build-macos:
    ./gradlew :shared:linkReleaseFrameworkMacosArm64
    @echo "✅ macOS framework: shared/build/bin/macosArm64/releaseFramework/"

# -----------------------------------------------------------------------------
# Builds the full macOS .app via xcodebuild. Requires the Xcode project to exist (see apps/macosApp/README.md).
build-macos-app: build-macos
    #!/usr/bin/env sh
    PROJ="apps/macosApp/macosApp.xcodeproj"
    if [ ! -d "$PROJ" ]; then
        echo "❌ Xcode project not found at $PROJ"
        echo "📖 See apps/macosApp/README.md for one-time setup"
        exit 1
    fi
    xcodebuild -project "$PROJ" \
               -scheme macosApp \
               -configuration Debug \
               -derivedDataPath apps/macosApp/build \
               build
    echo "✅ macOS app: apps/macosApp/build/Build/Products/Debug/macosApp.app"

# -----------------------------------------------------------------------------
# Builds the full iOS .app for the simulator via xcodebuild. Requires the Xcode project to exist (see apps/iosApp/README.md).
build-ios-app: build-ios
    #!/usr/bin/env sh
    PROJ="apps/iosApp/iosApp.xcodeproj"
    if [ ! -d "$PROJ" ]; then
        echo "❌ Xcode project not found at $PROJ"
        echo "📖 See apps/iosApp/README.md for one-time setup"
        exit 1
    fi
    xcodebuild -project "$PROJ" \
               -scheme iosApp \
               -configuration Debug \
               -destination "platform=iOS Simulator,name=iPhone 17 Pro" \
               -derivedDataPath apps/iosApp/build \
               build
    echo "✅ iOS app: apps/iosApp/build/Build/Products/Debug-iphonesimulator/iosApp.app"

# -----------------------------------------------------------------------------
# Builds everything that can build on this host.
build-all: build build-linux build-web build-android
    @echo "✅ Built shared + Linux + Web + Android"

# =============================================================================
# Compile-only checks (no linking)
# =============================================================================

# -----------------------------------------------------------------------------
# Compiles shared sources for Android (no APK).
compile-android:
    ./gradlew :shared:compileDebugKotlinAndroid

# -----------------------------------------------------------------------------
# Compiles shared sources for Linux native targets.
compile-linux:
    ./gradlew :shared:compileKotlinLinuxX64

# -----------------------------------------------------------------------------
# Compiles shared sources for Windows native target.
compile-windows:
    ./gradlew :shared:compileKotlinMingwX64

# -----------------------------------------------------------------------------
# Compiles shared sources for JS target.
compile-js:
    ./gradlew :shared:compileKotlinJs

# -----------------------------------------------------------------------------
# Compiles shared sources for iOS arm64 (Apple host only).
compile-ios:
    ./gradlew :shared:compileKotlinIosArm64

# -----------------------------------------------------------------------------
# Compiles shared sources for macOS arm64 (Apple host only).
compile-macos:
    ./gradlew :shared:compileKotlinMacosArm64

# -----------------------------------------------------------------------------
# Compiles shared sources for every target on this host.
compile-all: compile-android compile-linux compile-windows compile-js compile-ios compile-macos
    @echo "✅ Shared compiled for all targets"

# =============================================================================
# Run apps
# =============================================================================

# -----------------------------------------------------------------------------
# Runs the Linux CLI binary, building it first. Native on Linux, `docker run` on macOS/Windows.
run-linux *args: build-linux
    #!/usr/bin/env sh
    set -e
    OS=$(uname -s)
    if [ "$OS" = "Linux" ]; then
        case "$(uname -m)" in
            x86_64)        DIR=linuxX64   ;;
            aarch64|arm64) DIR=linuxArm64 ;;
        esac
        ./apps/linuxApp/build/bin/${DIR}/releaseExecutable/linuxApp.kexe {{args}}
    else
        docker run --rm --platform=linux/amd64 abkmyip-linux:latest {{args}}
    fi

# -----------------------------------------------------------------------------
# Runs the Windows CLI binary, building it first. Native exec on Windows; informational stub on macOS/Linux (no Wine; verify via CI instead).
run-windows *args: build-windows
    #!/usr/bin/env sh
    set -e
    OS=$(uname -s)
    EXE="apps/windowsApp/build/bin/mingwX64/releaseExecutable/windowsApp.exe"
    case "$OS" in
        MINGW*|MSYS*|CYGWIN*|Windows_NT)
            "./${EXE}" {{args}}
            ;;
        *)
            echo "ℹ️  Built: ${EXE}"
            echo "ℹ️  Windows .exe cannot run on ${OS}. Options:"
            echo "    • Copy to a Windows machine and run there"
            echo "    • Push to GitHub — the windows-build workflow smoke-tests the .exe on windows-latest"
            ;;
    esac

# -----------------------------------------------------------------------------
# Runs the web app dev server with hot reload at http://localhost:8080.
run-web:
    ./gradlew :apps:webApp:jsBrowserDevelopmentRun --continuous

# -----------------------------------------------------------------------------
# Installs and launches the Android app on a connected device or emulator. Boots the Pixel_10 AVD if no device is attached.
run-android:
    #!/usr/bin/env sh
    set -e
    AVD="Pixel_10"
    APP_ID="com.abkcompany.myip.androidApp"
    ADB="${ANDROID_HOME:-$HOME/Library/Android/sdk}/platform-tools/adb"
    EMULATOR="${ANDROID_HOME:-$HOME/Library/Android/sdk}/emulator/emulator"
    if [ ! -x "$ADB" ] || [ ! -x "$EMULATOR" ]; then
        echo "❌ Android SDK not found. Set ANDROID_HOME (currently: ${ANDROID_HOME:-unset})"
        exit 1
    fi
    if [ "$("$ADB" devices | awk 'NR>1 && $2=="device"' | wc -l | tr -d ' ')" = "0" ]; then
        echo "📱 No device attached — booting AVD: $AVD"
        "$EMULATOR" -avd "$AVD" -no-snapshot-save >/dev/null 2>&1 &
        "$ADB" wait-for-device
        echo "⏳ Waiting for boot to complete..."
        until [ "$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do
            sleep 2
        done
    fi
    ./gradlew :apps:androidApp:installDebug
    "$ADB" shell am start -n "$APP_ID/.MainActivity"
    echo "🚀 Launched: $APP_ID"

# -----------------------------------------------------------------------------
# Builds and launches the macOS app.
run-macos: build-macos-app
    #!/usr/bin/env sh
    APP="apps/macosApp/build/Build/Products/Debug/macosApp.app"
    if [ ! -d "$APP" ]; then
        echo "❌ Built app not found at $APP"
        exit 1
    fi
    open "$APP"
    echo "🚀 Launched: $APP"

# -----------------------------------------------------------------------------
# Builds and launches the iOS app on the iPhone 17 Pro simulator.
run-ios: build-ios-app
    #!/usr/bin/env sh
    DEVICE="iPhone 17 Pro"
    APP="apps/iosApp/build/Build/Products/Debug-iphonesimulator/iosApp.app"
    if [ ! -d "$APP" ]; then
        echo "❌ Built app not found at $APP"
        exit 1
    fi
    BUNDLE_ID=$(/usr/libexec/PlistBuddy -c "Print :CFBundleIdentifier" "$APP/Info.plist")
    xcrun simctl boot "$DEVICE" 2>/dev/null || true
    open -a Simulator
    xcrun simctl install "$DEVICE" "$APP"
    xcrun simctl launch "$DEVICE" "$BUNDLE_ID"
    echo "🚀 Launched: $APP on $DEVICE"

# -----------------------------------------------------------------------------
# Builds and launches the iOS app on the iPad Pro 11-inch (M5) simulator.
run-ipad-pro: build-ios-app
    #!/usr/bin/env sh
    DEVICE="iPad Pro 11-inch (M5)"
    APP="apps/iosApp/build/Build/Products/Debug-iphonesimulator/iosApp.app"
    if [ ! -d "$APP" ]; then
        echo "❌ Built app not found at $APP"
        exit 1
    fi
    BUNDLE_ID=$(/usr/libexec/PlistBuddy -c "Print :CFBundleIdentifier" "$APP/Info.plist")
    xcrun simctl boot "$DEVICE" 2>/dev/null || true
    open -a Simulator
    xcrun simctl install "$DEVICE" "$APP"
    xcrun simctl launch "$DEVICE" "$BUNDLE_ID"
    echo "🚀 Launched: $APP on $DEVICE"

# -----------------------------------------------------------------------------
# Builds and launches the iOS app on the iPad mini (A17 Pro) simulator.
run-ipad-mini: build-ios-app
    #!/usr/bin/env sh
    DEVICE="iPad mini (A17 Pro)"
    APP="apps/iosApp/build/Build/Products/Debug-iphonesimulator/iosApp.app"
    if [ ! -d "$APP" ]; then
        echo "❌ Built app not found at $APP"
        exit 1
    fi
    BUNDLE_ID=$(/usr/libexec/PlistBuddy -c "Print :CFBundleIdentifier" "$APP/Info.plist")
    xcrun simctl boot "$DEVICE" 2>/dev/null || true
    open -a Simulator
    xcrun simctl install "$DEVICE" "$APP"
    xcrun simctl launch "$DEVICE" "$BUNDLE_ID"
    echo "🚀 Launched: $APP on $DEVICE"

# -----------------------------------------------------------------------------
# Opens the macOS Xcode project in Xcode (or guides setup if it does not exist).
open-macos:
    #!/usr/bin/env sh
    PROJ="apps/macosApp/macosApp.xcodeproj"
    if [ ! -d "$PROJ" ]; then
        echo "❌ Xcode project not found at $PROJ"
        echo "📖 See apps/macosApp/README.md for one-time setup"
        exit 1
    fi
    open "$PROJ"

# =============================================================================
# Tests
# =============================================================================

# -----------------------------------------------------------------------------
# Runs the fast JVM unit tests on the shared module (default).
test:
    ./gradlew :shared:jvmTest

# -----------------------------------------------------------------------------
# Runs every host-runnable shared test.
test-all:
    ./gradlew :shared:allTests

# -----------------------------------------------------------------------------
# Runs shared JVM tests.
test-jvm:
    ./gradlew :shared:jvmTest

# -----------------------------------------------------------------------------
# Runs shared Android unit tests.
test-android:
    ./gradlew :shared:testDebugUnitTest

# -----------------------------------------------------------------------------
# Runs shared iOS tests (Apple host only).
test-ios:
    ./gradlew :shared:iosSimulatorArm64Test

# -----------------------------------------------------------------------------
# Runs shared macOS tests (Apple host only).
test-macos:
    ./gradlew :shared:macosArm64Test

# -----------------------------------------------------------------------------
# Runs shared Linux tests.
test-linux:
    ./gradlew :shared:linuxX64Test

# -----------------------------------------------------------------------------
# Runs shared Windows tests (mingwX64).
test-windows:
    ./gradlew :shared:mingwX64Test

# -----------------------------------------------------------------------------
# Runs shared JS tests.
test-js:
    ./gradlew :shared:jsTest

# -----------------------------------------------------------------------------
# Runs Linux CLI sanity tests.
test-linux-app:
    ./gradlew :apps:linuxApp:linuxX64Test

# =============================================================================
# Simulators / Emulators
# =============================================================================

# -----------------------------------------------------------------------------
# Opens the iOS Simulator application (Apple host only).
ios-simulator:
    @open -a Simulator

# -----------------------------------------------------------------------------
# Lists installed iOS simulators that are available to boot.
ios-sim-list:
    xcrun simctl list devices available

# -----------------------------------------------------------------------------
# Boots a named iOS simulator and opens the Simulator app.
ios-sim-boot device:
    xcrun simctl boot "{{device}}"
    open -a Simulator
    @echo "📱 Booted: {{device}}"

# -----------------------------------------------------------------------------
# Shuts down all booted iOS simulators.
ios-sim-shutdown:
    xcrun simctl shutdown all
    @echo "✅ All iOS simulators shut down"

# -----------------------------------------------------------------------------
# Starts the first available Android AVD in the background.
android-emulator *args:
    #!/usr/bin/env sh
    if [ -z "$ANDROID_HOME" ]; then
        echo "❌ ANDROID_HOME is not set. Install Android SDK and export ANDROID_HOME."
        exit 1
    fi
    avds=$("$ANDROID_HOME/emulator/emulator" -list-avds 2>/dev/null)
    if [ -z "$avds" ]; then
        echo "❌ No AVDs found. Create one in Android Studio or via 'avdmanager create avd'."
        exit 1
    fi
    avd=$(echo "$avds" | head -n 1)
    echo "🚀 Starting Android emulator: $avd"
    nohup "$ANDROID_HOME/emulator/emulator" -avd "$avd" {{args}} > /dev/null 2>&1 &
    echo "📱 Emulator booting in background. Check progress with: just android-devices"

# -----------------------------------------------------------------------------
# Lists configured Android Virtual Devices.
android-avds:
    #!/usr/bin/env sh
    if [ -z "$ANDROID_HOME" ]; then
        echo "❌ ANDROID_HOME is not set"
        exit 1
    fi
    "$ANDROID_HOME/emulator/emulator" -list-avds

# -----------------------------------------------------------------------------
# Lists Android devices and emulators currently visible to adb.
android-devices:
    adb devices -l

# =============================================================================
# Project tools
# =============================================================================

# -----------------------------------------------------------------------------
# Prints the current app version from the single source of truth.
version:
    @grep '^app = ' gradle/libs.versions.toml | cut -d '"' -f 2

# -----------------------------------------------------------------------------
# Shows the dependency tree for the shared module.
deps:
    ./gradlew :shared:dependencies

# -----------------------------------------------------------------------------
# Shows what pulls in a specific dependency (e.g. just deps-insight okhttp).
deps-insight dep:
    ./gradlew :shared:dependencyInsight --dependency "{{dep}}"

# -----------------------------------------------------------------------------
# Deletes all Gradle build outputs.
clean:
    ./gradlew clean
    @echo "🧹 Build outputs cleaned"

# -----------------------------------------------------------------------------
# Re-resolves dependencies from remote (bypasses cache).
refresh:
    ./gradlew build --refresh-dependencies

# =============================================================================
# Code style
# =============================================================================

# -----------------------------------------------------------------------------
# Auto-formats Kotlin sources with ktlint (requires ktlint on PATH).
format:
    #!/usr/bin/env sh
    if ! command -v ktlint > /dev/null 2>&1; then
        echo "❌ ktlint not installed. Install with: brew install ktlint"
        exit 1
    fi
    ktlint -F "shared/**/*.kt" "apps/**/*.kt" "tests/**/*.kt"

# -----------------------------------------------------------------------------
# Lints Kotlin sources with ktlint (requires ktlint on PATH).
lint:
    #!/usr/bin/env sh
    if ! command -v ktlint > /dev/null 2>&1; then
        echo "❌ ktlint not installed. Install with: brew install ktlint"
        exit 1
    fi
    ktlint "shared/**/*.kt" "apps/**/*.kt" "tests/**/*.kt"

# -----------------------------------------------------------------------------
# Quick test that just is installed and working.
test-just:
    @echo "✅ Just is working! Platform: {{os()}}"
    @echo "📁 Project root: {{justfile_directory()}}"
    @printf "📦 App version: "
    @grep '^app = ' gradle/libs.versions.toml | cut -d '"' -f 2
