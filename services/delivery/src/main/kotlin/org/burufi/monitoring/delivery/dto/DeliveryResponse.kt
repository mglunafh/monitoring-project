package org.burufi.monitoring.delivery.dto

data class DeliveryResponse<T : Payload>(val responseCode: ResponseCode, val errorMessage: String?, val payload: T?) {

    companion object {
        fun <T : Payload> error(responseCode: ResponseCode, errorMessage: String): DeliveryResponse<T> {
            require(responseCode != ResponseCode.OK) { "Error response must not be OK" }
            return DeliveryResponse(responseCode, errorMessage, null)
        }

        fun <T : Payload> T.toResponse() = DeliveryResponse(
            responseCode = ResponseCode.OK,
            errorMessage = null,
            payload = this
        )
    }
}
