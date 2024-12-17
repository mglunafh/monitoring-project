package org.burufi.monitoring.delivery.dto

import org.burufi.monitoring.delivery.model.OrderStatus
import java.time.LocalDateTime

data class DeliveryOrderDto(
    val id: Int,
    val shoppingCartId: String,
    val distance: Int,
    val transportMark: String,
    val orderTime: LocalDateTime,
    val status: OrderStatus,
    val departureTime: LocalDateTime?,
    val arrivalTime: LocalDateTime?
)
