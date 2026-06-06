import SwiftUI
import shared

struct ContentView: View {
    @State private var info: IpInfo? = nil
    @State private var mapUrl: URL? = nil
    @State private var error: String? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            if let e = error {
                Text("Error: \(e)").foregroundColor(.red)
            } else if let i = info {
                Text("IP: \(i.ip)").font(.title2).bold()
                Text("\(i.city), \(i.region)")
                Text("\(i.country) (\(i.countryCode))")
                Text("Timezone: \(i.timezone)")
                Text("Location: \(i.location.latitude), \(i.location.longitude)")
                if let url = mapUrl {
                    AsyncImage(url: url) { image in
                        image.resizable().scaledToFit()
                    } placeholder: {
                        ProgressView()
                    }
                    .frame(maxWidth: .infinity)
                }
            } else {
                ProgressView("Looking up your IP…")
            }
            Spacer()
        }
        .padding()
        .frame(minWidth: 480, minHeight: 360)
        .task { await load() }
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
