package org.burufi.monitoring.delivery.service

import org.burufi.monitoring.delivery.model.DeliveryOrder
import org.burufi.monitoring.delivery.service.OrderCapture.Companion.toCapture
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class BackgroundOrderManager(
    private val orderService: BackgroundOrderService,
    private val executor: BackgroundExecutor
) {

    private val tickRate = 1000L

    fun sendOrder(order: DeliveryOrder) {
        val transport = order.transport
        if (transport == null) return

        val orderCapture = order.toCapture()
        executor.execute { trackOrder(orderCapture) }
    }

    fun trackOrder(order: OrderCapture) {
        val orderId = order.orderId
        val transportId = order.transportId
        val speed = order.transportSpeed
        var distanceLeft = order.orderDistance
        var ticks = 0
        while (distanceLeft > 0) {
//            println("[order $orderId] '${order.mark}' #$transportId on its way, $distanceLeft left.")
            distanceLeft -= speed
            ticks += 1
            Thread.sleep(tickRate)
        }
        val totalTicks = ticks
        val totalCost = BigDecimal(ticks) * order.pricePerDistance

//        println("[order $orderId] Delivery cost: $totalCost")
        orderService.deliverOrder(orderId, transportId, totalCost)

        executor.execute {
            Thread.sleep(totalTicks * tickRate)
            val anotherOrder = orderService.returnTransport(transportId)
            anotherOrder?.also { executor.execute { trackOrder(it) } }
        }
    }
}
