package org.burufi.monitoring.delivery.dto

data class ErrorResponse(override val responseCode: ResponseCode, val errorMessage: String) : DeliveryResponse {

    init {
        require(responseCode != ResponseCode.OK) { "Error response must not be OK" }
    }
}
