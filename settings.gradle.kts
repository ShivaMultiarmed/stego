pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        mavenCentral()
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    val kotlinVersion = "2.1.20-RC3"
    val composeVersion = "1.5.0"

    plugins {
        kotlin("jvm").version(kotlinVersion)
        id("org.jetbrains.compose").version(composeVersion)
        id("org.jetbrains.kotlin.plugin.compose").version(kotlinVersion)
    }
}

rootProject.name = "Stego Project"