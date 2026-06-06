# Architecture

## Module dependency graph

```mermaid
graph TD
    androidApp[":apps:androidApp<br/>Jetpack Compose"]
    iosApp[":apps:iosApp<br/>SwiftUI (iOS + iPadOS)"]
    macosApp[":apps:macosApp<br/>SwiftUI (macOS)"]
    linuxApp[":apps:linuxApp<br/>Kotlin/Native CLI"]
    windowsApp[":apps:windowsApp<br/>Kotlin/Native CLI"]
    webApp[":apps:webApp<br/>Kotlin/JS"]
    shared[":shared<br/>KMP library — all business logic"]

    androidApp --> shared
    iosApp -.->|via XCFramework| shared
    macosApp -.->|via XCFramework| shared
    linuxApp --> shared
    windowsApp --> shared
    webApp --> shared
```

## Layered design inside `shared`

```mermaid
graph LR
    UI[Platform UI] --> UC[usecase]
    UC --> REPO[data.IpInfoRepository]
    REPO --> SVC[data.IpApiService]
    SVC -->|Ktor| API((ipapi.co))
    UC --> DOM[domain models]
    REPO --> DOM
```

- `domain/` — pure immutable data classes (`IpInfo`, `GeoLocation`, `StaticMapUrl`).
- `data/` — HTTP-layer (`IpApiService` via Ktor + kotlinx.serialization) and the repository that maps DTOs to domain types.
- `usecase/` — one entry point per user-facing operation (`GetMyIpInfoUseCase`, `BuildStaticMapUrlUseCase`).
- `platform/` — minimal `expect`/`actual` surface: `httpClient()` and `platformName`.
- `AbkMyIp` — application-composition root, exposes use cases ready to call.

## Request flow

```mermaid
sequenceDiagram
    participant UI as Platform UI
    participant App as AbkMyIp
    participant UC as GetMyIpInfoUseCase
    participant Repo as DefaultIpInfoRepository
    participant Svc as IpApiService (Ktor)
    participant API as ipapi.co

    UI->>App: getMyIpInfo()
    App->>UC: invoke()
    UC->>Repo: getMyIpInfo()
    Repo->>Svc: fetchIpInfo()
    Svc->>API: GET /json/
    API-->>Svc: JSON {ip, city, …}
    Svc-->>Repo: IpInfoDto
    Repo-->>UC: IpInfo (domain)
    UC-->>App: IpInfo
    App-->>UI: IpInfo
    UI->>App: buildStaticMapUrl(info.location)
    App-->>UI: StaticMapUrl (OSM static-tile URL)
    UI->>UI: render image and text
```

## Test layout

```mermaid
graph LR
    subgraph "shared/src/ (production)"
        CM[commonMain]
        AM[androidMain]
        JM[jvmMain]
        IM[iosMain]
        MM[macosMain]
        LM[linuxMain]
        WM[mingwMain]
        SM[jsMain]
    end
    subgraph "tests/shared/ (test) — relocated via kotlin.srcDir(...)"
        CT[commonTest]
        AT[androidUnitTest]
        JT[jvmTest]
        IT[iosTest]
        MT[macosTest]
        LT[linuxTest]
        WT[mingwTest]
        ST[jsTest]
    end
    CM -.test for.-> CT
    AM -.-> AT
    JM -.-> JT
    IM -.-> IT
    MM -.-> MT
    LM -.-> LT
    WM -.-> WT
    SM -.-> ST
```

Production binaries cannot pull in test code because `tests/` is not under any `src/` tree — Gradle source-set rewiring is the only path that exposes those files to compilation, and it is only configured for the `*Test` source sets.
