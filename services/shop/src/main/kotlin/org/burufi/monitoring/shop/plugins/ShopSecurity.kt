package org.burufi.monitoring.shop.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.burufi.monitoring.shop.service.CustomerService

private const val SHOP_AUTH = "shop-auth"

fun Application.configureSecurity(userService: CustomerService) {
    install(ContentNegotiation) {
        json()
    }

    authentication {
        basic(SHOP_AUTH) {
            realm = "Access to '/shop' path"

            validate { credentials ->
                hashedUserTable.authenticate(credentials)
            }
        }
    }

    routing {
        authenticate(SHOP_AUTH) {
            get("/shop/items") {
                call.respond("Hello world")
            }
        }

        get("/shop/users") {
            call.respond(userService.customers())
        }

        get("/shop/user") {
            val name = call.queryParameters["login"]

            val customer = name?.let { userService.customerByLogin(it) }
             if (customer == null) {
                 call.respond(HttpStatusCode.NotFound, "User not found")
             } else {
                 call.respond(customer)
             }
        }
    }
}

// TODO use BCrypt (https://github.com/patrickfav/bcrypt)
private val digestFunction = getDigestFunction("SHA-256") { "ipmn-shop-${it.length}" }

private val hashedUserTable = UserHashedTableAuth(
    table = mapOf(
        "admin" to digestFunction("minda"),
        "breadlover" to digestFunction("buns'n'rolls"),
        "terminally-ill" to digestFunction("painkiller")
    ),
    digester = digestFunction
)


