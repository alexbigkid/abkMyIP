plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kover)
}

val appVersion = libs.versions.app.get()
val generatedConfigDir = layout.buildDirectory.dir("generated/source/buildConfig/commonMain/kotlin")

fun loadIpinfoToken(): String {
    providers.environmentVariable("IPINFO_TOKEN").orNull?.takeIf { it.isNotBlank() }?.let { return it }
    val envFile = rootProject.file(".env")
    if (envFile.exists()) {
        envFile.readLines().forEach { raw ->
            val line = raw.trim()
            if (line.startsWith("#") || !line.startsWith("IPINFO_TOKEN=")) return@forEach
            val value = line.substringAfter("=").trim()
                .removeSurrounding("\"")
                .removeSurrounding("'")
            if (value.isNotBlank()) return value
        }
    }
    return ""
}

val ipinfoToken = loadIpinfoToken()
logger.lifecycle("ipinfo token: ${if (ipinfoToken.isBlank()) "absent (rate-limited free tier)" else "present"}")

val generateBuildConfig = tasks.register("generateBuildConfig") {
    val outFile = generatedConfigDir.get().file("com/abk/myip/BuildConfig.kt").asFile
    val versionString = appVersion
    val tokenString = ipinfoToken
    inputs.property("version", versionString)
    inputs.property("ipinfoToken", tokenString)
    outputs.file(outFile)
    doLast {
        outFile.parentFile.mkdirs()
        outFile.writeText(
            """
            |package com.abk.myip
            |
            |object BuildConfig {
            |    const val APP_VERSION = "$versionString"
            |    const val IPINFO_TOKEN = "$tokenString"
            |}
            |
            """.trimMargin()
        )
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    dependsOn(generateBuildConfig)
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
        commonMain {
            kotlin.srcDir(generatedConfigDir)
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.kermit)
            }
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
