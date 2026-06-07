import SwiftUI
import MapKit
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
    @State private var error: String? = nil

    var body: some View {
        NavigationStack {
            content
                .navigationTitle("My IP")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar { appearanceToolbarItem }
        }
        .task { await load() }
    }

    @ViewBuilder
    private var content: some View {
        if let error {
            ErrorView(message: error)
        } else if let info {
            GeometryReader { geo in
                let landscape = geo.size.width > geo.size.height
                if landscape {
                    HStack(spacing: 16) {
                        InfoPanel(info: info)
                        MapPanel(location: info.location)
                    }
                    .padding(.horizontal, 8)
                } else {
                    ScrollView {
                        VStack(spacing: 16) {
                            InfoPanel(info: info)
                            MapPanel(location: info.location)
                                .frame(height: 280)
                        }
                    }
                }
            }
        } else {
            ProgressView("Looking up your IP…")
                .controlSize(.large)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }

    @ToolbarContentBuilder
    private var appearanceToolbarItem: some ToolbarContent {
        ToolbarItem(placement: .topBarTrailing) {
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
            info = try await app.getMyIpInfo.invoke()
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
    let location: GeoLocation

    private var coord: CLLocationCoordinate2D {
        CLLocationCoordinate2D(
            latitude: location.latitude,
            longitude: location.longitude
        )
    }

    var body: some View {
        Map(initialPosition: .region(MKCoordinateRegion(
            center: coord,
            span: MKCoordinateSpan(latitudeDelta: 0.1, longitudeDelta: 0.1)
        ))) {
            Marker("My IP", coordinate: coord)
        }
        .clipShape(RoundedRectangle(cornerRadius: 12))
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
