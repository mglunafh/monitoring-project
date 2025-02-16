import org.gradle.kotlin.dsl.support.kotlinCompilerOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.springframework.boot")
}

subprojects {
    group = "org.burufi.monitoring"
    version = "0.3"

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.springframework.boot")

    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict", "-Xjvm-default=all-compatibility")
        }
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation(platform("org.springframework.boot:spring-boot-dependencies:3.4.0"))
        implementation(platform("org.testcontainers:testcontainers-bom:1.20.4"))
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.bootBuildImage {
        imageName = "monitoring-${project.name}:$version"

        environment = mapOf(
            "BP_JVM_VERSION" to "21",
            "BP_JVM_CDS_ENABLED" to "false",
            "BP_SPRING_CLOUD_BINDINGS_DISABLED" to "true",
            "BPL_SPRING_CLOUD_BINDINGS_DISABLED" to "true"
        )
    }
}

repositories {
    mavenCentral()
}
