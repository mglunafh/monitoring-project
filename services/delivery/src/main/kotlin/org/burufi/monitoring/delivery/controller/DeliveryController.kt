package org.burufi.monitoring.delivery.controller

import jakarta.validation.Valid
import org.burufi.monitoring.delivery.dto.CreateDeliveryOrderDto
import org.burufi.monitoring.delivery.dto.CreatedDeliveryOrder
import org.burufi.monitoring.delivery.dto.DeliveryResponse
import org.burufi.monitoring.delivery.dto.DeliveryResponse.Companion.toResponse
import org.burufi.monitoring.delivery.dto.ListOrderResponse
import org.burufi.monitoring.delivery.dto.OrderStatisticsResponse
import org.burufi.monitoring.delivery.dto.ResponseCode
import org.burufi.monitoring.delivery.mapper.OrderMapper
import org.burufi.monitoring.delivery.service.DeliveryOrderService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.Errors
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/delivery")
class DeliveryController(
    private val orderService: DeliveryOrderService
) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createDeliveryOrder(@Valid @RequestBody orderDto: CreateDeliveryOrderDto, errors: Errors): ResponseEntity<DeliveryResponse<CreatedDeliveryOrder>> {
        if (errors.hasErrors()) {
            val message = errors.allErrors.joinToString(separator = ". ") { it.defaultMessage ?: "" }
            return ResponseEntity.badRequest().body(DeliveryResponse.error(ResponseCode.VALIDATION_FAILURE, message))
        }
        val createdOrder = orderService.create(orderDto)
        val response = CreatedDeliveryOrder(createdOrder.id!!, createdOrder.orderTime).toResponse()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/ongoing", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun showOngoingOrders(): ResponseEntity<DeliveryResponse<ListOrderResponse>> {
        val ongoingOrders = orderService.getOngoing()
        return ResponseEntity.ok(ListOrderResponse(ongoingOrders).toResponse())
    }

    @GetMapping("/stats", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun showStatistics(): ResponseEntity<DeliveryResponse<OrderStatisticsResponse>> {
        val statistics = orderService.getStatistics()
        val statisticsDto = statistics.map { OrderMapper.map(it) }
        return ResponseEntity.ok(OrderStatisticsResponse(statisticsDto).toResponse())
    }
}
