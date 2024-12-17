package org.burufi.monitoring.delivery.dto

import com.fasterxml.jackson.annotation.JsonFormat
import org.burufi.monitoring.delivery.model.OrderStatus
import java.time.LocalDateTime

data class DeliveryOrderDto(
    val id: Int,
    val shoppingCartId: String,
    val distance: Int,
    val transportMark: String,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    val orderTime: LocalDateTime,

    val status: OrderStatus,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    val departureTime: LocalDateTime?,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    val arrivalTime: LocalDateTime?
)
