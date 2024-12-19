package org.burufi.monitoring.delivery.model

import java.math.BigDecimal

interface OrderStatistics {
    val status: OrderStatus
    val orderCount: Int
    val totalCost: BigDecimal?
}
