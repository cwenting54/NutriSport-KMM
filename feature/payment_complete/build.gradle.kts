import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("nutrisport.kmp.feature")
                }

kotlin {

    sourceSets {

        commonMain.dependencies {

            implementation(project(path = ":shared"))
            implementation(project(path = ":data"))
            implementation(project(path = ":feature:home:cart"))
            implementation(project(path = ":feature:home:cart:checkout"))
            implementation(project(path = ":feature:home:products_overview"))
            implementation(project(path = ":feature:home:categories"))
            implementation(project(path = ":feature:home:categories:category_search"))
        }
    }
}

android {
    namespace = "dev.nutrisport.payment_complete"
}
