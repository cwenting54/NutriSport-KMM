import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import com.codingfeline.buildkonfig.gradle.BuildKonfigExtension

plugins {
    id("nutrisport.kmp.feature")
                    alias(libs.plugins.serialization)
    alias(libs.plugins.buildKonfig)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        load(file.inputStream())
    }
}

configure<BuildKonfigExtension> {
    packageName = "dev.nutrisport.shared"

    defaultConfigs {
        val clientId = localProperties.getProperty("paypal.client.id") ?: ""
        val secretId = localProperties.getProperty("paypal.secret.id") ?: ""

        buildConfigField(FieldSpec.Type.STRING, "PAYPAL_CLIENT_ID", clientId)
        buildConfigField(FieldSpec.Type.STRING, "PAYPAL_SECRET_ID", secretId)
    }
}

kotlin {

    sourceSets {
        commonMain.dependencies {

            implementation(libs.kotlinx.serialization)
            implementation(libs.kotlinx.datetime)

            implementation(libs.koin.core)

            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.multiplatform.settings.make.observable)

            implementation(libs.coil3)
            implementation(libs.coil3.compose)
            implementation(libs.coil3.compose.core)
            implementation(libs.coil3.network.ktor)
        }
    }
}

android {
    namespace = "dev.nutrisport.shared"
}


dependencies {
    implementation(libs.kotlinx.datetime)
}
