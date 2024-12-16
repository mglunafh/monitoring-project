package org.burufi.monitoring.delivery.controller

import jakarta.validation.Valid
import org.burufi.monitoring.delivery.dto.CreateDeliveryOrderDto
import org.burufi.monitoring.delivery.dto.CreateDeliveryOrderResponse
import org.burufi.monitoring.delivery.dto.DeliveryResponse
import org.burufi.monitoring.delivery.dto.ErrorResponse
import org.burufi.monitoring.delivery.dto.ResponseCode
import org.burufi.monitoring.delivery.service.DeliveryOrderService
import org.springframework.http.ResponseEntity
import org.springframework.validation.Errors
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/delivery")
class DeliveryController(
    val orderService: DeliveryOrderService
) {

    @PostMapping
    fun createDeliveryOrder(@Valid @RequestBody orderDto: CreateDeliveryOrderDto, errors: Errors): ResponseEntity<DeliveryResponse> {
        if (errors.hasErrors()) {
            val message = errors.allErrors.joinToString(separator = ". ") { it.defaultMessage }
            return ResponseEntity.badRequest().body(ErrorResponse(ResponseCode.VALIDATION_FAILURE, message))
        }
        val createdOrder = orderService.create(orderDto)

        return ResponseEntity.ok(CreateDeliveryOrderResponse(createdOrder.id!!, createdOrder.orderTime))
    }
}
