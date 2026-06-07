import SwiftUI

@main
struct iosAppApp: App {
    @AppStorage("appearance") private var appearance: Appearance = .dark

    var body: some Scene {
        WindowGroup {
            ContentView(appearance: $appearance)
                .preferredColorScheme(appearance.colorScheme)
        }
    }
}
