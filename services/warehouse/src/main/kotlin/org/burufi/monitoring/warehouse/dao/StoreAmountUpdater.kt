package org.burufi.monitoring.warehouse.dao

import org.burufi.monitoring.dto.warehouse.ContractItemOrderDto
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import java.sql.PreparedStatement

/**
 * Batch update object which updates the amount of the available goods in the warehouse
 *      after the contract with the supplier has been struck.
 */
data class StoreAmountUpdater(val items: List<ContractItemOrderDto>) : BatchPreparedStatementSetter {

    companion object {
        const val UPDATE_ITEM_AMOUNT_QUERY = "update goods set amount = amount + ? where id = ?"
    }

    override fun setValues(ps: PreparedStatement, i: Int) {
        val item = items[i]
        ps.setInt(1, item.amount)
        ps.setInt(2, item.id)
    }

    override fun getBatchSize() = items.size
}
