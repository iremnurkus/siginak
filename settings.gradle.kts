pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
        plugins {
            id("org.jetbrains.kotlin.android") version "2.0.0" apply false
            id("org.jetbrains.kotlin.jvm") version "2.0.0" apply false
            id("org.jetbrains.kotlin.kapt") version "2.0.0" apply false
            id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
        }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "siginak"
include(":app")
