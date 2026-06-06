plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    mingwX64 {
        binaries.executable {
            entryPoint = "com.abk.myip.windows.main"
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        mingwMain.dependencies {
            implementation(project(":shared"))
        }
        mingwTest {
            kotlin.srcDir(rootProject.file("tests/apps/windowsApp/mingwTest/kotlin"))
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
