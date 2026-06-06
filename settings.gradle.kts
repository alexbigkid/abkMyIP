rootProject.name = "abkMyIP"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

include(":shared")
include(":apps:androidApp")
include(":apps:linuxApp")
include(":apps:windowsApp")
include(":apps:webApp")
