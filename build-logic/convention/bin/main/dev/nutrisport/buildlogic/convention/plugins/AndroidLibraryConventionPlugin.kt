package dev.nutrisport.buildlogic.convention.plugins

import com.android.build.api.dsl.LibraryExtension
import dev.nutrisport.buildlogic.convention.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 35
                
                // testOptions.animationsDisabled = true
            }

            dependencies {
                // androidTestImplementation(libs.findLibrary("kotlin.test").get())
                // testImplementation(libs.findLibrary("kotlin.test").get())
            }
        }
    }
}
