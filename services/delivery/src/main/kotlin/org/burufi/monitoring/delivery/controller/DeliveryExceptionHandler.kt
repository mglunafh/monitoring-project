package org.burufi.monitoring.delivery.controller

import org.burufi.monitoring.delivery.dto.ErrorResponse
import org.burufi.monitoring.delivery.dto.ResponseCode
import org.burufi.monitoring.delivery.exception.DeliveryException
import org.burufi.monitoring.delivery.exception.FailureType.SHOPPING_CART_ID_ALREADY_EXISTS
import org.burufi.monitoring.delivery.exception.FailureType.TRANSPORT_MARK_NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class DeliveryExceptionHandler {

    @ExceptionHandler
    fun businessException(ex: DeliveryException ): ResponseEntity<ErrorResponse> {
        return when (ex.failure) {
            TRANSPORT_MARK_NOT_FOUND ->
                ResponseEntity.badRequest().body(ErrorResponse(ResponseCode.VALIDATION_FAILURE,
                    "Incorrect transport mark. Check if it's in the list of available transport types."))

            SHOPPING_CART_ID_ALREADY_EXISTS -> ResponseEntity.badRequest().body(ErrorResponse(ResponseCode.VALIDATION_FAILURE,
                "Given shopping cart is already registered for delivery."))
        }
    }
}
