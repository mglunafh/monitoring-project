package org.burufi.monitoring.delivery.mapper

import org.burufi.monitoring.delivery.model.DeliveryOrder
import org.burufi.monitoring.delivery.model.OrderStatistics
import org.burufi.monitoring.dto.delivery.DeliveryOrderDto
import org.burufi.monitoring.dto.delivery.OrderStatisticsDto

object OrderMapper {

    fun map(order: DeliveryOrder) = DeliveryOrderDto(
        id = order.id!!,
        shoppingCartId = order.shoppingCartId,
        distance = order.distance,
        transportMark = order.transportType.mark,
        orderTime = order.orderTime,
        status = order.status.name,
        departureTime = order.departureTime,
        arrivalTime = order.arrivalTime
    )

    fun map(orderStatistics: OrderStatistics) = OrderStatisticsDto(
        status = orderStatistics.status.name,
        orderCount = orderStatistics.orderCount,
        totalCost = orderStatistics.totalCost,
    )
}
