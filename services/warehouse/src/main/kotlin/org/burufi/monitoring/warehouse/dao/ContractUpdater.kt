package org.burufi.monitoring.warehouse.dao

import org.burufi.monitoring.dto.warehouse.ContractItemOrderDto
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import java.sql.PreparedStatement

/**
 * Batch update object which registers the supply items of the contract.
 */
data class ContractUpdater(
    val contractId: Int,
    val items: List<ContractItemOrderDto>
) : BatchPreparedStatementSetter {

    companion object {
        const val INSERT_ITEM_QUERY = "insert into goods_in_contract values(?, ?, ?, ?)"
    }

    override fun setValues(ps: PreparedStatement, i: Int) {
        val item = items[i]
        ps.setInt(1, contractId)
        ps.setInt(2, item.id)
        ps.setInt(3, item.amount)
        ps.setBigDecimal(4, item.price)
    }

    override fun getBatchSize() = items.size

}
