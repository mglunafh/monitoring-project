package org.burufi.monitoring.dto.delivery

import org.burufi.monitoring.dto.Payload
import java.time.LocalDateTime

sealed interface DeliveryPayload : Payload

data class CreatedDeliveryOrder(val shoppingCartId: String, val orderId: Int, val orderTime: LocalDateTime) : DeliveryPayload

data class ListOrders(val orders: List<DeliveryOrderDto>) : DeliveryPayload

data class OrderStatistics(val statistics: List<OrderStatisticsDto>) : DeliveryPayload
