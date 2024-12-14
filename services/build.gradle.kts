plugins {
    kotlin("jvm")
}

subprojects {
    group = "org.burufi.monitoring"
    version = "0.1"

    apply(plugin = "org.jetbrains.kotlin.jvm")

    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    repositories {
        mavenCentral()
    }
}
