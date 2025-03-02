package org.burufi.monitoring.shop.dao

import kotlinx.coroutines.Dispatchers
import org.burufi.monitoring.shop.model.Customer
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class CustomerRepositoryImpl : CustomerRepository {

    override suspend fun all() = suspendTransaction {
        CustomerTable.selectAll().map { CustomerTable.convert(it) }
    }

    override suspend fun customerById(id: Int) = suspendTransaction {
        CustomerTable.selectAll()
            .where { CustomerTable.id eq id }
            .map { CustomerTable.convert(it) }
            .singleOrNull()
    }

    override suspend fun customerByName(name: String) = suspendTransaction {
        CustomerTable.selectAll()
            .where { CustomerTable.login eq name }
            .map { CustomerTable.convert(it) }
            .singleOrNull()
    }

    override suspend fun addUser(customer: Customer) = suspendTransaction {
        TODO("Not yet implemented")
    }
}

private suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

