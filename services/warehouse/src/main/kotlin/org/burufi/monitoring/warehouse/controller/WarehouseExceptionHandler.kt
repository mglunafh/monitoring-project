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
        const val TOO_FEW_ITEMS_CURRENTLY = "Currently there are too few items to reserve. " +
                "Please order the smaller amount or try again later."

        const val UNEXPECTED_SERVER_ERROR = "Unexpected server error."

        private val log = LoggerFactory.getLogger(WarehouseExceptionHandler::class.java)
    }

    @ExceptionHandler
    fun businessException(ex: WarehouseException): ResponseEntity<MyResponse<Nothing>> {
        val response : MyResponse<Nothing> = when (val failure = ex.failure) {
            FailureType.SupplierIdNotFound -> MyResponse.error(VALIDATION_FAILURE, SUPPLIER_ID_NOT_REGISTERED)

            is FailureType.ProductIdNotFound -> MyResponse.error(
                VALIDATION_FAILURE,
                "Non-existent product ID was mentioned in the contract. ${failure.message ?: ""}")

            FailureType.ReserveTooManyItems -> MyResponse.error(INTERNAL_SERVER_ERROR, TOO_FEW_ITEMS_CURRENTLY)

            is FailureType.GenericDatabaseFailure -> MyResponse.error(
                INTERNAL_SERVER_ERROR,
                failure.cause.message ?: "Unspecified database error")
        }
        return when (response.responseCode) {
            ResponseCode.OK -> ResponseEntity.ok(response)
            ResponseCode.NOT_FOUND -> ResponseEntity.status(404).body(response)
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
