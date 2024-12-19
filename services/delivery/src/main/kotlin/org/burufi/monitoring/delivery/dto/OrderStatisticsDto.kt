package org.burufi.monitoring.delivery.dto

import org.burufi.monitoring.delivery.model.OrderStatus
import java.math.BigDecimal

data class OrderStatisticsDto(
    val status: OrderStatus,
    val orderCount: Int,
    val totalCost: BigDecimal?
)
