package org.burufi.monitoring.delivery.dto

sealed interface DeliveryResponse {
    val responseCode: ResponseCode
}
