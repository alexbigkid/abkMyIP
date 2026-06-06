plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    linuxX64 {
        binaries.executable {
            entryPoint = "com.abk.myip.linux.main"
        }
    }
    linuxArm64 {
        binaries.executable {
            entryPoint = "com.abk.myip.linux.main"
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        linuxMain.dependencies {
            implementation(project(":shared"))
        }
        linuxTest {
            kotlin.srcDir(rootProject.file("tests/apps/linuxApp/linuxTest/kotlin"))
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
