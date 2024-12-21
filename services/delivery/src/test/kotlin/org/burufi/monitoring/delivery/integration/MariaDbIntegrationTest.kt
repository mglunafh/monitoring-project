package org.burufi.monitoring.delivery.integration

import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.jdbc.Sql
import org.testcontainers.containers.MariaDBContainer

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test-mariadb")
@Sql(scripts = ["classpath:data.sql"])
class MariaDbIntegrationTest : DeliveryIntegrationTest() {

    companion object {
        val mariadb = MariaDBContainer("mariadb:latest")

        @JvmStatic
        @DynamicPropertySource
        fun configDatabase(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", mariadb::getJdbcUrl)
            registry.add("spring.datasource.username", mariadb::getUsername)
            registry.add("spring.datasource.password", mariadb::getPassword)
        }

        @JvmStatic
        @BeforeAll
        fun startDatabase() {
            mariadb.start()
        }

        @JvmStatic
        @BeforeAll
        fun stopDatabase() {
            mariadb.stop()
        }
    }
}
