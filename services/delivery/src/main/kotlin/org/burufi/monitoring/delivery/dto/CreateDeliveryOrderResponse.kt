package org.burufi.monitoring.delivery.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class CreateDeliveryOrderResponse(
    val orderId: Int,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    val orderTime: LocalDateTime
) : DeliveryResponse {
    override val responseCode = ResponseCode.OK
}
