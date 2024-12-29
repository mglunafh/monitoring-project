package org.burufi.monitoring.delivery.dto

import java.time.LocalDateTime

sealed class Payload

data class CreatedDeliveryOrder(val shoppingCartId: String, val orderId: Int, val orderTime: LocalDateTime) : Payload()

data class ListOrderResponse(val orders: List<DeliveryOrderDto>) : Payload()

data class OrderStatisticsResponse(val statistics: List<OrderStatisticsDto>) : Payload()
