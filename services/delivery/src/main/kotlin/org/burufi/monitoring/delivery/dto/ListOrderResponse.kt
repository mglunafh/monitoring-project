package org.burufi.monitoring.delivery.dto

data class ListOrderResponse(val orders: List<DeliveryOrderDto>) : DeliveryResponse {
    override val responseCode = ResponseCode.OK
}
