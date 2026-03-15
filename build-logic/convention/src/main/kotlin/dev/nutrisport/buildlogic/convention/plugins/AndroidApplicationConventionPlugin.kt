package dev.nutrisport.buildlogic.convention.plugins

import com.android.build.api.dsl.ApplicationExtension
import dev.nutrisport.buildlogic.convention.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                if (!hasPlugin("org.jetbrains.kotlin.multiplatform")) {
                    apply("org.jetbrains.kotlin.android")
                }
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                testOptions.animationsDisabled = true

                defaultConfig.applicationId = "dev.nutrisport.app"
                defaultConfig.versionCode = 1
                defaultConfig.versionName = "1.0"

                buildTypes.getByName("release").isMinifyEnabled = false
            }
        }
    }
}
