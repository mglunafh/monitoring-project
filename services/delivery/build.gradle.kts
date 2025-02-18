plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.springBoot)
}

dependencies {
    implementation(platform(libs.bom.springBoot))
    testImplementation(platform(libs.bom.testcontainers))

    implementation(project(":dto"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2:2.3.232")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test"))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mariadb")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.assertj:assertj-core")
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
