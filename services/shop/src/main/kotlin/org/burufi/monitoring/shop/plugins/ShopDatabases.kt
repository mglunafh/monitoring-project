package org.burufi.monitoring.shop.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabases() {
    val config = HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://localhost:5434/db_shop"
        username = "db-user"
        password = "db-pwd"
        schema = "shop"
        connectionTimeout = 1000
    }
    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)
}
