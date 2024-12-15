pluginManagement {
    plugins {
        kotlin("jvm") version "2.1.0"
        kotlin("plugin.spring") version "2.1.0"
        kotlin("plugin.jpa") version "2.1.0"
        id("org.springframework.boot") version "3.4.0"
    }

    repositories {
        mavenCentral()
    }
}

rootProject.name = "monitoring-services"
include("delivery")
include("shop")
