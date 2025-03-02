package org.burufi.monitoring.shop.dao

import org.burufi.monitoring.shop.model.Customer
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object CustomerTable : Table("customers") {
    val id = integer("id")
    val login = varchar("login", 30)
    val registeredAt = datetime("registered_at")
    val password = varchar("password", 70)

    fun convert(result: ResultRow) = Customer(
        id = result[id],
        login = result[login],
        registeredAt = result[registeredAt],
        password = result[password]
    )
}


