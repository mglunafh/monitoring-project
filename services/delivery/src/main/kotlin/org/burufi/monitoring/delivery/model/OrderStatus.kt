package org.burufi.monitoring.delivery.model

/**
 * Type of the status of a delivery order.
 */
enum class OrderStatus {
    /**
     * Delivery order is registered.
     */
    REGISTERED,

    /**
     * Delivery order is on its way to the destination.
     */
    SENT,

    /**
     * Order is delivered.
     */
    DELIVERED
}
