plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "abkMyIp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        jsMain {
            dependencies {
                implementation(project(":shared"))
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        jsTest {
            kotlin.srcDir(rootProject.file("tests/apps/webApp/jsTest/kotlin"))
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
