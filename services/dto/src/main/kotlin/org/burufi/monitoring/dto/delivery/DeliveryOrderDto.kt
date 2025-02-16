package org.burufi.monitoring.dto.delivery

import java.time.LocalDateTime

data class DeliveryOrderDto(
    val id: Int,
    val shoppingCartId: String,
    val distance: Int,
    val transportMark: String,
    val orderTime: LocalDateTime,
    val status: String,     // order status
    val departureTime: LocalDateTime?,
    val arrivalTime: LocalDateTime?
)
