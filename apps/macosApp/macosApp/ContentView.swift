import SwiftUI
import shared

enum Appearance: String, CaseIterable, Identifiable {
    case system, light, dark
    var id: String { rawValue }

    var colorScheme: ColorScheme? {
        switch self {
        case .system: return nil
        case .light: return .light
        case .dark: return .dark
        }
    }

    var iconName: String {
        switch self {
        case .system: return "circle.lefthalf.fill"
        case .light: return "sun.max.fill"
        case .dark: return "moon.fill"
        }
    }
}

struct ContentView: View {
    @Binding var appearance: Appearance
    @State private var info: IpInfo? = nil
    @State private var mapUrl: URL? = nil
    @State private var error: String? = nil

    var body: some View {
        content
            .frame(minWidth: 720, minHeight: 440)
            .toolbar { appearanceToolbarItem }
            .task { await load() }
    }

    @ViewBuilder
    private var content: some View {
        if let error {
            ErrorView(message: error)
        } else if let info {
            HSplitView {
                InfoPanel(info: info)
                MapPanel(url: mapUrl)
            }
        } else {
            ProgressView("Looking up your IP…")
                .controlSize(.large)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }

    @ToolbarContentBuilder
    private var appearanceToolbarItem: some ToolbarContent {
        ToolbarItem(placement: .primaryAction) {
            Menu {
                Picker("Appearance", selection: $appearance) {
                    Label("System", systemImage: "circle.lefthalf.fill").tag(Appearance.system)
                    Label("Light",  systemImage: "sun.max.fill").tag(Appearance.light)
                    Label("Dark",   systemImage: "moon.fill").tag(Appearance.dark)
                }
                .pickerStyle(.inline)
            } label: {
                Image(systemName: appearance.iconName)
            }
        }
    }

    private func load() async {
        do {
            let app = AbkMyIp()
            let fetched = try await app.getMyIpInfo.invoke()
            info = fetched
            let urlString = app.buildStaticMapUrl.invoke(location: fetched.location).value
            mapUrl = URL(string: urlString)
        } catch {
            self.error = "\(error)"
        }
    }
}

struct InfoPanel: View {
    let info: IpInfo

    var body: some View {
        VStack(alignment: .leading, spacing: 20) {
            Text(info.ip)
                .font(.system(.largeTitle, design: .monospaced).bold())
                .lineLimit(1)
                .minimumScaleFactor(0.5)
                .textSelection(.enabled)

            Text("\(info.city), \(info.region) · \(info.countryCode)")
                .font(.title2)
                .foregroundStyle(.secondary)

            Divider()

            VStack(alignment: .leading, spacing: 14) {
                LabeledContent("Timezone") { Text(info.timezone) }
                if let org = info.org, !org.isEmpty {
                    LabeledContent("ISP") {
                        Text(org).multilineTextAlignment(.trailing)
                    }
                }
                LabeledContent("Coordinates") { Text(coordsText) }
            }
            .font(.body)

            Spacer()
        }
        .padding(24)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
    }

    private var coordsText: String {
        String(format: "%.4f, %.4f", info.location.latitude, info.location.longitude)
    }
}

struct MapPanel: View {
    let url: URL?

    var body: some View {
        Group {
            if let url {
                AsyncImage(url: url) { image in
                    image.resizable().scaledToFit()
                } placeholder: {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                }
            } else {
                Color.clear
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding(24)
    }
}

struct ErrorView: View {
    let message: String

    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.largeTitle)
                .foregroundStyle(.red)
            Text("Couldn't fetch your IP info")
                .font(.headline)
            Text(message)
                .font(.caption)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
        }
        .padding()
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
