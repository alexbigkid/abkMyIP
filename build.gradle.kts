plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.detekt) apply false
}

subprojects {
    if (!projectDir.resolve("src").exists()) return@subprojects

    apply(plugin = "io.gitlab.arturbosch.detekt")

    val moduleTestsDir = rootProject.file(
        "tests/" + project.path.removePrefix(":").replace(":", "/")
    )

    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        autoCorrect = false
        parallel = true
        ignoreFailures = true
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        val sources = mutableListOf<Any>("src")
        if (moduleTestsDir.exists()) sources += moduleTestsDir
        setSource(files(sources))
        include("**/*.kt")
        exclude("**/build/**", "**/generated/**", "**/resources/**")
        reports {
            html.required.set(false)
            xml.required.set(false)
            md.required.set(false)
            txt.required.set(false)
            sarif.required.set(false)
        }
    }
}
