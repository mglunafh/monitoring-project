package org.burufi.monitoring.delivery.service

import org.burufi.monitoring.delivery.model.DeliveryOrder
import java.math.BigDecimal

data class OrderCapture(
    val orderId: Int,
    val orderDistance: Int,
    val transportId: Int,
    val transportSpeed: Int,
    val mark: String,
    val pricePerDistance: BigDecimal
) {

    companion object {
        fun DeliveryOrder.toCapture(): OrderCapture {
            val orderId = requireNotNull(id)
            val transport = requireNotNull(transport)
            val transportId = requireNotNull(transport.id)
            val transportType = transportType

            return OrderCapture(
                orderId = orderId,
                orderDistance = distance,
                transportId = transportId,
                transportSpeed = transportType.speed,
                mark = transportType.mark,
                pricePerDistance = transportType.pricePerDistance
            )
        }
    }
}
