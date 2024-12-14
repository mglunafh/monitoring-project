package org.burufi.monitoring.delivery.model

/**
 * Status of the transport.
 */
enum class TransportStatus {

    /**
     * Transport unit is in the park and ready to deliver the orders.
     */
    AVAILABLE,

    /**
     * Transport unit is on the way to deliver an order.
     */
    DELIVERING,

    /**
     * Transport unit is on its way back to the park.
     */
    RETURNING
}