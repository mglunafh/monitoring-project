package org.burufi.monitoring.delivery.dto

import java.time.LocalDateTime

data class CreateDeliveryOrderResponse(
    val orderId: Int,
    val orderTime: LocalDateTime
) : DeliveryResponse {
    override val responseCode = ResponseCode.OK
}
