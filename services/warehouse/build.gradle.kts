plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
}

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    implementation(project(":dto"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test"))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")

    mockitoAgent("org.mockito:mockito-core:5.14.2") {     // Mockito version taken from the Spring Boot BOM 3.4.0
        isTransitive = false
    }
}

tasks.test {
    // Loads mockito-core library as a Java agent during test start-up
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
}
