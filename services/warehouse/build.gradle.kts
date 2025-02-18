plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.springBoot)
}

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    implementation(platform(libs.bom.springBoot))
    testImplementation(platform(libs.bom.testcontainers))

    implementation(project(":dto"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test"))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.assertj:assertj-core")
    testImplementation(libs.mockito.kotlin)

    mockitoAgent(libs.mockito) {
        isTransitive = false
    }
}

tasks.test {
    // Loads mockito-core library as a Java agent during test start-up
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
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
