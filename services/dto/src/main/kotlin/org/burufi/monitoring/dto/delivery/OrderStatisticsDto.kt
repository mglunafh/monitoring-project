package org.burufi.monitoring.dto.delivery

import java.math.BigDecimal

data class OrderStatisticsDto(
    val status: String,     // order status
    val orderCount: Int,
    val totalCost: BigDecimal?
)
