package dev.nutrisport.buildlogic.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidCompose() {
    val composePlugin = "org.jetbrains.compose"
    val kotlinComposePlugin = "org.jetbrains.kotlin.plugin.compose"

    with(pluginManager) {
        apply(composePlugin)
        apply(kotlinComposePlugin)
    }

    dependencies {
        val composeRuntime = libs.findLibrary("compose-runtime").get()
        val composeFoundation = libs.findLibrary("compose-foundation").get()
        val composeMaterial3 = libs.findLibrary("compose-material3").get()
        val composeUi = libs.findLibrary("compose-ui").get()
        val lifecycleViewModelCompose = libs.findLibrary("androidx-lifecycle-viewmodelCompose").get()
        val lifecycleRuntimeCompose = libs.findLibrary("androidx-lifecycle-runtimeCompose").get()
        val composeNavigation = libs.findLibrary("compose-navigation").get()

        // Wait, compose dependencies are accessed differently in KMP (via compose.*). 
        // In KMP projects, we don't use standard dependencies {} block for commonMain,
        // we configure sourceSets in `kotlin { }` extension.
        // If we want purely Android, we use dependencies.
        // Actually since we use Compose Multiplatform, this should be done differently!
    }
}
