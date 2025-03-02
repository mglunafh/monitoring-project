package org.burufi.monitoring.shop

import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.burufi.monitoring.shop.dao.CustomerRepositoryImpl
import org.burufi.monitoring.shop.plugins.configureDatabases
import org.burufi.monitoring.shop.plugins.configureSecurity
import org.burufi.monitoring.shop.service.CustomerService

fun main(args: Array<String>) = EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    configureDatabases()

    val customerService = CustomerService(CustomerRepositoryImpl())
    configureSecurity(customerService)
}
