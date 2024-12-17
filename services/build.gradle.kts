import org.gradle.kotlin.dsl.support.kotlinCompilerOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
        compilerOptions {
            optIn.add("-Xjsr305=strict")    // TODO not sure where it belongs exactly....
        }
    }

    repositories {
        mavenCentral()
    }

    tasks.test {
        useJUnitPlatform()
    }
}

repositories {
    mavenCentral()
}
