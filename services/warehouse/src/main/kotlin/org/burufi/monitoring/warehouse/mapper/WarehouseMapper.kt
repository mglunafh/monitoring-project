package org.burufi.monitoring.warehouse.mapper

import org.burufi.monitoring.dto.warehouse.GoodsItemDto
import org.burufi.monitoring.dto.warehouse.SupplierDto
import org.burufi.monitoring.warehouse.dao.record.GoodsItem
import org.burufi.monitoring.warehouse.dao.record.Supplier

object WarehouseMapper {

    fun map(supplier: Supplier) = SupplierDto(
        id = supplier.id,
        name = supplier.name,
        description = supplier.description
    )

    fun map(item: GoodsItem) = GoodsItemDto(
        id = item.id,
        name = item.name,
        category = item.category.name,
        amount = item.amount.n,
        weight = item.weight
    )
}
