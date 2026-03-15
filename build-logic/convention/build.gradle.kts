import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "dev.nutrisport.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    runtimeOnly(libs.compose.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "nutrisport.android.application"
            implementationClass = "dev.nutrisport.buildlogic.convention.plugins.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "nutrisport.android.library"
            implementationClass = "dev.nutrisport.buildlogic.convention.plugins.AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "nutrisport.android.library.compose"
            implementationClass = "dev.nutrisport.buildlogic.convention.plugins.AndroidLibraryComposeConventionPlugin"
        }
        register("multiplatformCommon") {
            id = "nutrisport.multiplatform.common"
            implementationClass = "dev.nutrisport.buildlogic.convention.plugins.MultiplatformCommonConventionPlugin"
        }
        register("koin") {
            id = "nutrisport.koin"
            implementationClass = "dev.nutrisport.buildlogic.convention.plugins.KoinConventionPlugin"
        }
        register("kmpFeature") {
            id = "nutrisport.kmp.feature"
            implementationClass = "dev.nutrisport.buildlogic.convention.plugins.KmpFeatureConventionPlugin"
        }
    }
}
