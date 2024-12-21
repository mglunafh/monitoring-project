package org.burufi.monitoring.delivery.controller

import org.burufi.monitoring.delivery.dto.DeliveryResponse
import org.burufi.monitoring.delivery.dto.ResponseCode
import org.burufi.monitoring.delivery.exception.DeliveryException
import org.burufi.monitoring.delivery.exception.FailureType.SHOPPING_CART_ID_ALREADY_EXISTS
import org.burufi.monitoring.delivery.exception.FailureType.TRANSPORT_MARK_NOT_FOUND
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class DeliveryExceptionHandler {

    companion object {
        const val INCORRECT_TRANSPORT_MARK = "Incorrect transport mark. Check if it's in the list of available transport types."
        const val SHOPPING_CARD_ALREADY_REGISTERED = "Given shopping cart is already registered for delivery."
        const val UNEXPECTED_SERVER_ERROR = "Unexpected server error."

        private val log = LoggerFactory.getLogger(DeliveryExceptionHandler::class.java)
    }

    @ExceptionHandler
    fun businessException(ex: DeliveryException): ResponseEntity<DeliveryResponse<*>> {
        return when (ex.failure) {
            TRANSPORT_MARK_NOT_FOUND -> ResponseEntity.badRequest().body(
                DeliveryResponse.error<Nothing>(ResponseCode.VALIDATION_FAILURE, INCORRECT_TRANSPORT_MARK))

            SHOPPING_CART_ID_ALREADY_EXISTS -> ResponseEntity.badRequest().body(
                DeliveryResponse.error<Nothing>(ResponseCode.VALIDATION_FAILURE, SHOPPING_CARD_ALREADY_REGISTERED))
        }
    }

    @ExceptionHandler
    fun internalServerError(ex: Exception): ResponseEntity<DeliveryResponse<*>> {
        log.error("Encountered error during request processing", ex)
        return ResponseEntity.internalServerError().body(
            DeliveryResponse.error<Nothing>(ResponseCode.INTERNAL_SERVER_ERROR, UNEXPECTED_SERVER_ERROR))
    }
}
