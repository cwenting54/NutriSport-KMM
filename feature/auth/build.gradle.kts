import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("nutrisport.kmp.feature")
                }

kotlin {

    sourceSets {

        commonMain.dependencies {

            implementation(libs.auth.kmp)
            implementation(libs.auth.firebase.kmp)

            implementation(project(path = ":shared"))
            implementation(project(path = ":data"))
        }
    }
}

android {
    namespace = "dev.nutrisport.auth"
}
