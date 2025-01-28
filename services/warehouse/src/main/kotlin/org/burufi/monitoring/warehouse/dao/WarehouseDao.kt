package org.burufi.monitoring.warehouse.dao

import org.burufi.monitoring.dto.warehouse.ContractInfo
import org.burufi.monitoring.dto.warehouse.ContractItemOrderDto
import org.burufi.monitoring.warehouse.dao.record.GoodsItem
import org.burufi.monitoring.warehouse.dao.record.ReservationDetails
import org.burufi.monitoring.warehouse.dao.record.ReservationFullDetails
import org.burufi.monitoring.warehouse.dao.record.ReserveStatus
import org.burufi.monitoring.warehouse.dao.record.ReserveStatus.CANCELLED
import org.burufi.monitoring.warehouse.dao.record.ReserveStatus.PAID
import org.burufi.monitoring.warehouse.dao.record.Supplier
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime

@Component
class WarehouseDao(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    private val keyColumns = arrayOf("id")

    fun getGoodsList(): List<GoodsItem> {
        return jdbcTemplate.query("select * from goods", RowMappers.GoodsItemRowMapper)
    }

    fun getSuppliersList(): List<Supplier> {
        return jdbcTemplate.query("select * from suppliers", RowMappers.SupplierRowMapper)
    }

    fun supplierExists(id: Int): Boolean {
        return jdbcTemplate.queryForObject(
            "select count(1) as count from suppliers where id = :id",
            mapOf("id" to id),
            RowMappers.RecordExistsRowMapper
        ) == 1
    }

    fun itemExists(id: Int): Boolean {
        return jdbcTemplate.queryForObject(
            "select count(1) as count from goods where id = :id",
            mapOf("id" to id),
            RowMappers.RecordExistsRowMapper
        ) == 1
    }

    fun createContract(supplierId: Int, signDate: LocalDateTime, cost: BigDecimal): Int {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update(
            "insert into supply_contracts(supplier_id, sign_date, total_cost) values(:id, :date, :cost)",
            MapSqlParameterSource(mapOf("id" to supplierId, "date" to signDate,  "cost" to cost)),
            keyHolder,
            keyColumns
        )
        return keyHolder.key as Int
    }

    fun registerContractItems(contractId: Int, items: List<ContractItemOrderDto>) {
        val baseJdbcTemplate = jdbcTemplate.jdbcTemplate
        baseJdbcTemplate.batchUpdate(ContractUpdater.INSERT_ITEM_QUERY, ContractUpdater(contractId, items))
        baseJdbcTemplate.batchUpdate(StoreAmountUpdater.UPDATE_ITEM_AMOUNT_QUERY, StoreAmountUpdater(items))
    }

    fun getContractInfo(contractId: Int): ContractInfo? {
        val result = jdbcTemplate.query(
            RowMappers.ContractInfoMapper.CONTRACT_INFO_QUERY,
            mapOf("id" to contractId),
            RowMappers.ContractInfoMapper
        )
        return if (result.isNotEmpty()) result[0] else null
    }

    fun reserveItem(shoppingCartId: String, id: Int, amount: Int, reserveTime: LocalDateTime) {
        jdbcTemplate.update(
            "update goods set amount = amount - :amount where id = :id",
            mapOf("id" to id, "amount" to amount)
        )
        jdbcTemplate.update("""
            insert into goods_reserve(shopping_cart_id, item_id, amount, action_time, status) values
                (:shoppingCardId, :itemId, :amount, :actionTime, :status)
        """.trimIndent(),
            mapOf(
                "shoppingCardId" to shoppingCartId,
                "itemId" to id,
                "amount" to amount,
                "actionTime" to reserveTime,
                "status" to ReserveStatus.RESERVED.name
            )
        )
    }

    fun isReservationProcessed(shoppingCartId: String): Boolean {
        val reservationSummary = jdbcTemplate.query(
            """
            select shopping_cart_id,status,count(status) from goods_reserve
                where shopping_cart_id = :id
                group by shopping_cart_id,status
            """.trimIndent(),
            mapOf("id" to shoppingCartId),
            RowMappers.ReservationSummaryRowMapper
        )

        return when {
            reservationSummary.isEmpty() -> true
            reservationSummary.any { (it.status == CANCELLED || it.status == PAID) && it.count > 0 } -> true
            else -> false
        }
    }

    fun cancelReservation(shoppingCartId: String, cancelTime: LocalDateTime): List<ReservationDetails> {
        val itemsReserved = jdbcTemplate.query(
            """
                SELECT shopping_cart_id, item_id, SUM(amount) as amount FROM goods_reserve
                    WHERE status = 'RESERVED' AND shopping_cart_id = :id
                    GROUP BY shopping_cart_id, item_id
            """.trimIndent(),
            mapOf("id" to shoppingCartId),
            RowMappers.ReservationDetailsRowMapper
        )

        if (itemsReserved.isEmpty()) return listOf()

        val baseJdbcTemplate = jdbcTemplate.jdbcTemplate
        baseJdbcTemplate.batchUpdate(
            ProcessReservationUpdater.UPDATE_ITEM_RESERVATION,
            ProcessReservationUpdater(shoppingCartId, cancelTime, itemsReserved, CANCELLED))
        baseJdbcTemplate.batchUpdate(
            CancelReservationStoreUpdater.RESTORE_ITEM_AMOUNT_QUERY,
            CancelReservationStoreUpdater(itemsReserved))

        return itemsReserved
    }

    fun purchaseReservation(shoppingCartId: String, purchaseTime: LocalDateTime): List<ReservationDetails> {
        val itemsReserved = jdbcTemplate.query(
            """
                SELECT shopping_cart_id, item_id, SUM(amount) as amount FROM goods_reserve
                    WHERE status = 'RESERVED' AND shopping_cart_id = :id
                    GROUP BY shopping_cart_id, item_id
            """.trimIndent(),
            mapOf("id" to shoppingCartId),
            RowMappers.ReservationDetailsRowMapper
        )

        if (itemsReserved.isEmpty()) return listOf()
        jdbcTemplate.jdbcTemplate.batchUpdate(
            ProcessReservationUpdater.UPDATE_ITEM_RESERVATION,
            ProcessReservationUpdater(shoppingCartId, purchaseTime, itemsReserved, PAID)
        )

        return itemsReserved
    }

    fun getReservationInfo(shoppingCartId: String): List<ReservationFullDetails> {
        return jdbcTemplate.query(
            """
                SELECT item_id, SUM(amount) as amount, MIN(action_time) as first_modified, MAX(action_time) as last_modified, status
                    FROM goods_reserve
                    WHERE shopping_cart_id = :id
                    GROUP BY item_id, status
                    ORDER BY status, item_id;
            """.trimIndent(),
            mapOf("id" to shoppingCartId),
            RowMappers.ReservationFullDetailsRowMapper
        )
    }
}
