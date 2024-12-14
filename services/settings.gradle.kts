pluginManagement {
    plugins {
        kotlin("jvm") version "2.1.0"
        application
    }

    repositories {
        mavenCentral()
    }
}

rootProject.name = "monitoring-services"
include("delivery")
include("shop")
