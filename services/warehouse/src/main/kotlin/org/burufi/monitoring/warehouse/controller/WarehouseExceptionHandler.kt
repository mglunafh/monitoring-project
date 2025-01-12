package org.burufi.monitoring.warehouse.controller

import org.burufi.monitoring.dto.MyResponse
import org.burufi.monitoring.dto.ResponseCode
import org.burufi.monitoring.warehouse.exception.FailureType
import org.burufi.monitoring.warehouse.exception.WarehouseException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class WarehouseExceptionHandler {

    companion object {
        const val UNEXPECTED_SERVER_ERROR = "Unexpected server error."

        private val log = LoggerFactory.getLogger(WarehouseExceptionHandler::class.java)
    }

    @ExceptionHandler
    fun businessException(ex: WarehouseException) {
        when (ex.failure) {
            FailureType.SUPPLIER_ID_NOT_FOUND -> TODO()
            FailureType.PRODUCT_ID_NOT_FOUND -> TODO()
        }
    }

    @ExceptionHandler
    fun internalServerError(ex: Exception): ResponseEntity<MyResponse<*>> {
        log.error("Encountered error during request processing", ex)
        return ResponseEntity.internalServerError().body(
            MyResponse.error<Nothing>(ResponseCode.INTERNAL_SERVER_ERROR, UNEXPECTED_SERVER_ERROR))
    }
}
