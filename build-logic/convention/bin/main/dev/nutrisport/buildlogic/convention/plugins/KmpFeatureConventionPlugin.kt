package dev.nutrisport.buildlogic.convention.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

class KmpFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("nutrisport.multiplatform.common")
                apply("nutrisport.android.library.compose")
                apply("nutrisport.koin")
            }
        }
    }
}
