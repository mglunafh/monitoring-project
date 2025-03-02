package org.burufi.monitoring.shop.dao

import org.burufi.monitoring.shop.model.Customer

interface CustomerRepository {

    suspend fun all(): List<Customer>

    suspend fun customerById(id: Int): Customer?

    suspend fun customerByName(name: String): Customer?

    suspend fun addUser(customer: Customer)

}
