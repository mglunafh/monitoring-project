package org.burufi.monitoring.warehouse.dao.record

import java.math.BigDecimal

data class GoodsItem(
    val id: Int,
    val name: String,
    val category: ItemType,
    val amount: Amount,
    val weight: BigDecimal
)
