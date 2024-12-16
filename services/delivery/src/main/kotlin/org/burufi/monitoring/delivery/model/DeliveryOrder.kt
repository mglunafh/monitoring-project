package org.burufi.monitoring.delivery.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

/**
 * Details of a delivery order.
 *
 * @property shoppingCartId ID of the shopping cart.
 * @property distance Distance from the delivery to the destination point.
 * @property transportType Transport type which was selected for the order, points to the [TransportType].
 * @property orderTime Time when the delivery order was registered.
 * @property status Order status of type [OrderStatus]
 * @property transport Transport unit assigned to this delivery order, points to the [Transport].
 * @property departureTime Departure time of the transport unit with the order.
 * @property arrivalTime Arrival time of the order.
 *
 * @see TransportType
 * @see Transport
 * @see OrderStatus
 */
@Entity
data class DeliveryOrder(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Int? = null,

    @Column(unique = true)
    var shoppingCartId: String,

    var distance: Int,

    @ManyToOne
    var transportType: TransportType,

    var orderTime: LocalDateTime,

    var status: OrderStatus = OrderStatus.REGISTERED,

    @ManyToOne
    var transport: Transport? = null,

    var departureTime: LocalDateTime? = null,

    var arrivalTime: LocalDateTime? = null
)
