package org.burufi.monitoring.delivery.mapper

import org.burufi.monitoring.delivery.dto.DeliveryOrderDto
import org.burufi.monitoring.delivery.model.DeliveryOrder

object OrderMapper {

    fun map(order: DeliveryOrder): DeliveryOrderDto {
        return DeliveryOrderDto(
            id = order.id!!,
            shoppingCartId = order.shoppingCartId,
            distance = order.distance,
            transportMark = order.transportType.mark,
            orderTime = order.orderTime,
            status = order.status,
            departureTime = order.departureTime,
            arrivalTime = order.arrivalTime
        )
    }
}
