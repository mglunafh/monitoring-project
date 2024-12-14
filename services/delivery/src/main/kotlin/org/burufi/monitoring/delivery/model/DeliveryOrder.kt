package org.burufi.monitoring.delivery.model

import java.time.LocalDateTime

/**
 * Details of a delivery order.
 *
 * @property shoppingCartId ID of the shopping cart.
 * @property distance Distance from the delivery to the destination point.
 * @property transportType Transport type which was selected for the order, points to the [TransportType].
 * @property orderTime Time when the delivery order was registered.
 * @property status Order status of type [OrderStatus]
 * @property transportId Transport unit assigned to this delivery order, points to the [TransportPark].
 * @property departureTime Departure time of the transport unit with the order.
 * @property arrivalTime Arrival time of the order.
 *
 * @see TransportType
 * @see TransportPark
 * @see OrderStatus
 */
data class DeliveryOrder(
    val id: Int,
    val shoppingCartId: String,
    val distance: Int,
    val transportType: Int,
    val orderTime: LocalDateTime,
    var status: OrderStatus,
    var transportId: Int,
    var departureTime: LocalDateTime?,
    var arrivalTime: LocalDateTime?
)
