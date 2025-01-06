package org.burufi.monitoring.warehouse.dao

import org.burufi.monitoring.warehouse.dao.record.Amount
import org.burufi.monitoring.warehouse.dao.record.GoodsItem
import org.burufi.monitoring.warehouse.dao.record.ItemType
import org.burufi.monitoring.warehouse.dao.record.Supplier
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

object RowMappers {

    object SupplierRowMapper : RowMapper<Supplier> {
        override fun mapRow(rs: ResultSet, rowNum: Int): Supplier {
            val id = rs.getInt("id")
            val name = rs.getString("name")
            val description = rs.getString("description")
            return Supplier(id, name, description)
        }
    }

    object GoodsItemRowMapper : RowMapper<GoodsItem> {
        override fun mapRow(rs: ResultSet, rowNum: Int): GoodsItem {
            val id = rs.getInt("id")
            val name = rs.getString("name")
            val category = ItemType.valueOf(rs.getString("category"))
            val amount = Amount(rs.getInt("amount"))
            val weight = rs.getBigDecimal("weight")
            return GoodsItem(id, name, category, amount, weight)
        }
    }
}
