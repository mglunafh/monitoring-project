package org.burufi.monitoring.warehouse.dao

import org.burufi.monitoring.dto.warehouse.ContractInfo
import org.burufi.monitoring.warehouse.dao.record.Amount
import org.burufi.monitoring.warehouse.dao.record.GoodsItem
import org.burufi.monitoring.warehouse.dao.record.ItemType
import org.burufi.monitoring.warehouse.dao.record.Supplier
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

object RowMappers {

    /**
     * Extracts the integer value of 'count' column of a query result.
     */
    object SupplierExistsRowMapper : RowMapper<Int> {
        override fun mapRow(rs: ResultSet, rowNum: Int) = rs.getInt("count")
    }

    /**
     *  Extracts the supplier records from the query result.
     */
    object SupplierRowMapper : RowMapper<Supplier> {
        override fun mapRow(rs: ResultSet, rowNum: Int): Supplier {
            val id = rs.getInt("id")
            val name = rs.getString("name")
            val description = rs.getString("description")
            return Supplier(id, name, description)
        }
    }

    /**
     * Extracts the items from the query result.
     */
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

    /**
     * Extracts the information about a contract with the given ID. Shall be used along with the [CONTRACT_INFO_QUERY].
     */
    object ContractInfoMapper : RowMapper<ContractInfo> {

        /**
         * Query for extracting the available information about a contract with the given id.
         * Accepts a single parameter `"id"` in the parameter source map.
         */
        val CONTRACT_INFO_QUERY = """
            select sc.id, suppliers.name, sc.sign_date, sc.total_cost from supply_contracts as sc
                join suppliers on supplier_id = suppliers.id
                where sc.id = :id;
            """.trimIndent()

        override fun mapRow(rs: ResultSet, rowNum: Int): ContractInfo? {
            val id = rs.getInt("id")
            val supplierName = rs.getString("name")
            val signDate = rs.getTimestamp("sign_date")
            val contractCost = rs.getBigDecimal("total_cost")
            return ContractInfo(id, supplierName, signDate.toLocalDateTime(), contractCost)
        }
    }
}
