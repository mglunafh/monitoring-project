package org.burufi.monitoring.delivery.integration

import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.jdbc.Sql
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test-postgresql")
@Sql(scripts = ["classpath:data.sql"])
class PostgresIntegrationTest : DeliveryIntegrationTest() {

    companion object {
        val postgresql = PostgreSQLContainer("postgres:latest")

        @JvmStatic
        @DynamicPropertySource
        fun configDatabase(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresql::getJdbcUrl)
            registry.add("spring.datasource.username", postgresql::getUsername)
            registry.add("spring.datasource.password", postgresql::getPassword)
        }

        @JvmStatic
        @BeforeAll
        fun startDatabase() {
            postgresql.start()
        }

        @JvmStatic
        @BeforeAll
        fun stopDatabase() {
            postgresql.stop()
        }
    }
}
