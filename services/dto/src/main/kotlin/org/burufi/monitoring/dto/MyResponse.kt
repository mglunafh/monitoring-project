package org.burufi.monitoring.dto

data class MyResponse<T : Payload>(val responseCode: ResponseCode, val errorMessage: String?, val payload: T?) {

    companion object {
        fun <T: Payload> error(responseCode: ResponseCode, errorMessage: String): MyResponse<T> {
            require(responseCode != ResponseCode.OK) { "Error response must not be OK" }
            return MyResponse(responseCode, errorMessage, null)
        }

        fun <T : Payload> T.toResponse() = MyResponse(
            responseCode = ResponseCode.OK,
            errorMessage = null,
            payload = this
        )
    }
}
