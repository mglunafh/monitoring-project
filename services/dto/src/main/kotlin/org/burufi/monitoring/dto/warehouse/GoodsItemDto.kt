package org.burufi.monitoring.dto.warehouse

import java.math.BigDecimal

data class GoodsItemDto(
    val id: Int,
    val name: String,
    val category: String, // item type
    val amount: Int,
    val weight: BigDecimal
)
