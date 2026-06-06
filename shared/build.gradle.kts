plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    macosX64()
    macosArm64()

    linuxX64()
    linuxArm64()

    mingwX64()

    js(IR) {
        browser()
        nodejs()
        binaries.executable()
    }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    listOf(macosX64(), macosArm64()).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kermit)
        }
        commonTest {
            kotlin.srcDir(rootProject.file("tests/shared/commonTest/kotlin"))
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.ktor.client.mock)
            }
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        getByName("androidUnitTest") {
            kotlin.srcDir(rootProject.file("tests/shared/androidUnitTest/kotlin"))
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        jvmTest {
            kotlin.srcDir(rootProject.file("tests/shared/jvmTest/kotlin"))
        }

        appleMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        iosTest { kotlin.srcDir(rootProject.file("tests/shared/iosTest/kotlin")) }
        macosTest { kotlin.srcDir(rootProject.file("tests/shared/macosTest/kotlin")) }

        linuxMain.dependencies {
            implementation(libs.ktor.client.curl)
        }
        linuxTest { kotlin.srcDir(rootProject.file("tests/shared/linuxTest/kotlin")) }

        mingwMain.dependencies {
            implementation(libs.ktor.client.winhttp)
        }
        mingwTest { kotlin.srcDir(rootProject.file("tests/shared/mingwTest/kotlin")) }

        jsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        jsTest { kotlin.srcDir(rootProject.file("tests/shared/jsTest/kotlin")) }
    }
}

android {
    namespace = "com.abk.myip.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
