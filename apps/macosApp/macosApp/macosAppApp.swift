import SwiftUI

@main
struct macosAppApp: App {
    @AppStorage("appearance") private var appearance: Appearance = .dark

    var body: some Scene {
        WindowGroup {
            ContentView(appearance: $appearance)
                .preferredColorScheme(appearance.colorScheme)
        }
    }
}
