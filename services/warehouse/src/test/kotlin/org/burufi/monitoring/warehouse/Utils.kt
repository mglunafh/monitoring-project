package org.burufi.monitoring.warehouse

import org.burufi.monitoring.warehouse.dao.RowMappers
import org.burufi.monitoring.warehouse.dao.record.GoodsItem
import org.burufi.monitoring.warehouse.dao.record.Supplier
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder

object Utils {

    fun NamedParameterJdbcTemplate.save(supplier: Supplier): Int {
        val keyHolder = GeneratedKeyHolder()
        this.update(
            "insert into suppliers(name, description) values (:name, :description)",
            MapSqlParameterSource(mapOf("name" to supplier.name, "description" to supplier.description)),
            keyHolder,
            arrayOf("id")
        )
        return keyHolder.key as Int
    }

    fun NamedParameterJdbcTemplate.save(item: GoodsItem): Int {
        val keyHolder = GeneratedKeyHolder()
        this.update("insert into goods(name, category, amount, weight) values (:n, :c, :a, :w)",
            MapSqlParameterSource(mapOf("n" to item.name, "c" to item.category.name, "a" to item.amount.n, "w" to item.weight)),
            keyHolder,
            arrayOf("id")
        )
        return keyHolder.key as Int
    }

    fun NamedParameterJdbcTemplate.persistedItem(id: Int): GoodsItem {
        return this.queryForObject("select * from goods where id = :id", mapOf("id" to id), RowMappers.GoodsItemRowMapper)!!
    }
}
