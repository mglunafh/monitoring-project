package org.burufi.monitoring.delivery.dto

data class OrderStatisticsResponse(val statistics: List<OrderStatisticsDto>) : DeliveryResponse {
    override val responseCode = ResponseCode.OK
}
