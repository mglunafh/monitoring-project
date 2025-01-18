package org.burufi.monitoring.warehouse.controller

import org.burufi.monitoring.dto.MyResponse
import org.burufi.monitoring.dto.ResponseCode
import org.burufi.monitoring.dto.ResponseCode.INTERNAL_SERVER_ERROR
import org.burufi.monitoring.dto.ResponseCode.VALIDATION_FAILURE
import org.burufi.monitoring.warehouse.exception.FailureType
import org.burufi.monitoring.warehouse.exception.WarehouseException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class WarehouseExceptionHandler {

    companion object {
        const val SUPPLIER_ID_NOT_REGISTERED = "Non-existent supplier ID was used in the contract."
        const val PRODUCT_ID_NOT_REGISTERED = "Non-existent product ID was mentioned in the contract."

        const val UNEXPECTED_SERVER_ERROR = "Unexpected server error."

        private val log = LoggerFactory.getLogger(WarehouseExceptionHandler::class.java)
    }

    @ExceptionHandler
    fun businessException(ex: WarehouseException): ResponseEntity<MyResponse<Nothing>> {
        val response : MyResponse<Nothing> = when (ex.failure) {
            FailureType.SUPPLIER_ID_NOT_FOUND -> MyResponse.error(VALIDATION_FAILURE, SUPPLIER_ID_NOT_REGISTERED)
            FailureType.PRODUCT_ID_NOT_FOUND -> MyResponse.error(VALIDATION_FAILURE, PRODUCT_ID_NOT_REGISTERED)
            FailureType.GENERIC_DATABASE_FAILURE -> MyResponse.error(
                INTERNAL_SERVER_ERROR,
                ex.details ?: "Unspecified database error")
        }
        return when (response.responseCode) {
            ResponseCode.OK -> ResponseEntity.ok(response)
            VALIDATION_FAILURE -> ResponseEntity.badRequest().body(response)
            INTERNAL_SERVER_ERROR -> ResponseEntity.internalServerError().body(response)
        }
    }

    @ExceptionHandler
    fun internalServerError(ex: Exception): ResponseEntity<MyResponse<*>> {
        log.error("Encountered error during request processing", ex)
        return ResponseEntity.internalServerError().body(
            MyResponse.error<Nothing>(INTERNAL_SERVER_ERROR, UNEXPECTED_SERVER_ERROR))
    }
}
