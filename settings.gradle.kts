
rootProject.name = "ktor-objectify"

pluginManagement {

    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }

    resolutionStrategy {
        val kotlinVersion: String by settings

        plugins {
            kotlin("jvm") version kotlinVersion
        }
    }
}