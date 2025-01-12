package org.burufi.monitoring.delivery.controller

import jakarta.validation.Valid
import org.burufi.monitoring.delivery.mapper.OrderMapper
import org.burufi.monitoring.delivery.service.DeliveryOrderService
import org.burufi.monitoring.dto.MyResponse
import org.burufi.monitoring.dto.MyResponse.Companion.toResponse
import org.burufi.monitoring.dto.ResponseCode
import org.burufi.monitoring.dto.delivery.CreateDeliveryOrderDto
import org.burufi.monitoring.dto.delivery.CreatedDeliveryOrder
import org.burufi.monitoring.dto.delivery.ListOrders
import org.burufi.monitoring.dto.delivery.OrderStatistics
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
    fun createDeliveryOrder(@Valid @RequestBody orderDto: CreateDeliveryOrderDto, errors: Errors): ResponseEntity<MyResponse<CreatedDeliveryOrder>> {
        if (errors.hasErrors()) {
            val message = errors.allErrors.joinToString(separator = ". ") { it.defaultMessage ?: "" }
            return ResponseEntity.badRequest().body(MyResponse.error(ResponseCode.VALIDATION_FAILURE, message))
        }
        val createdOrder = orderService.create(orderDto)
        val response = CreatedDeliveryOrder(orderDto.shoppingCartId, createdOrder.id!!, createdOrder.orderTime).toResponse()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/ongoing", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun showOngoingOrders(): ResponseEntity<MyResponse<ListOrders>> {
        val ongoingOrders = orderService.getOngoing()
        return ResponseEntity.ok(ListOrders(ongoingOrders).toResponse())
    }

    @GetMapping("/stats", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun showStatistics(): ResponseEntity<MyResponse<OrderStatistics>> {
        val statistics = orderService.getStatistics()
        val statisticsDto = statistics.map { OrderMapper.map(it) }
        return ResponseEntity.ok(OrderStatistics(statisticsDto).toResponse())
    }
}
