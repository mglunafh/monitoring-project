package org.burufi.monitoring.warehouse.dao

import org.burufi.monitoring.warehouse.dao.record.ReservationDetails
import org.burufi.monitoring.warehouse.dao.record.ReserveStatus
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.time.LocalDateTime

data class ProcessReservationUpdater(
    val shoppingCartId: String,
    val cancelDate: LocalDateTime,
    val reservations: List<ReservationDetails>,
    val status: ReserveStatus
) : BatchPreparedStatementSetter {

    companion object {
        const val UPDATE_ITEM_RESERVATION = """
            insert into goods_reserve(shopping_cart_id, item_id, amount, action_time, status) values (?, ?, ?, ?, ?)
        """
    }

    override fun setValues(ps: PreparedStatement, i: Int) {
        val reservation = reservations[i]
        ps.setString(1, shoppingCartId)
        ps.setInt(2, reservation.itemId)
        ps.setInt(3, reservation.amount.n)
        ps.setTimestamp(4, Timestamp.valueOf(cancelDate))
        ps.setString(5, status.name)
    }

    override fun getBatchSize() = reservations.size
}
