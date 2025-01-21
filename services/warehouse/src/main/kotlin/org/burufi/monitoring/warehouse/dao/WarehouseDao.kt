package org.burufi.monitoring.warehouse.dao

import org.burufi.monitoring.dto.warehouse.ContractInfo
import org.burufi.monitoring.dto.warehouse.ContractItemOrderDto
import org.burufi.monitoring.warehouse.dao.record.GoodsItem
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
            RowMappers.SupplierExistsRowMapper
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
}
