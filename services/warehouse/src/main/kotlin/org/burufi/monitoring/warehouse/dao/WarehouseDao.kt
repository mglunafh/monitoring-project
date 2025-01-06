package org.burufi.monitoring.warehouse.dao

import org.burufi.monitoring.warehouse.dao.record.GoodsItem
import org.burufi.monitoring.warehouse.dao.record.Supplier
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

@Component
class WarehouseDao(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun getGoodsList(): List<GoodsItem> {
        return jdbcTemplate.query("select * from goods", RowMappers.GoodsItemRowMapper)
    }

    fun getSuppliersList(): List<Supplier> {
        return jdbcTemplate.query("select * from suppliers", RowMappers.SupplierRowMapper)
    }
}
