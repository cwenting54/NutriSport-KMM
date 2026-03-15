import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("nutrisport.kmp.feature")
                    alias(libs.plugins.serialization)
}

kotlin {

    sourceSets {
        androidMain.dependencies {
            implementation(libs.ktor.android.client)
        }

        iosMain.dependencies {
            implementation(libs.ktor.darwin.client)
        }

        commonMain.dependencies {

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization)

            implementation(libs.browser.kmp)

            implementation(project(path = ":shared"))
            implementation(project(path = ":data"))
        }
    }
}

android {
    namespace = "dev.nutrisport.checkout"
}
