package org.burufi.monitoring.delivery.exception

import org.burufi.monitoring.delivery.model.OrderStatus
import org.burufi.monitoring.delivery.model.TransportStatus

/**
 * Types of errors expected to be seen during the background execution.
 */
sealed interface BackgroundFailureType {

    data class OrderIdNotFound(val orderId: Int) : BackgroundFailureType

    data class TransportIdNotFound(val transportId: Int) : BackgroundFailureType

    data class UnexpectedOrderStatus(val expected: OrderStatus, val actual: OrderStatus) : BackgroundFailureType

    data class UnexpectedTransportStatus(val expected: TransportStatus, val actual: TransportStatus) : BackgroundFailureType

    fun asException() = DeliveryBackgroundException(this)
}
