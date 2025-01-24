package org.burufi.monitoring.warehouse.dao

import org.burufi.monitoring.warehouse.dao.record.ReservationDetails
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import java.sql.PreparedStatement

data class CancelReservationStoreUpdater(val reservations: List<ReservationDetails>) : BatchPreparedStatementSetter {

    companion object {
        const val RESTORE_ITEM_AMOUNT_QUERY = "update goods set amount = amount + ? where id = ?"
    }

    override fun setValues(ps: PreparedStatement, i: Int) {
        val reservation = reservations[i]
        ps.setInt(1, reservation.amount.n)
        ps.setInt(2, reservation.itemId)
    }

    override fun getBatchSize() = reservations.size
}
