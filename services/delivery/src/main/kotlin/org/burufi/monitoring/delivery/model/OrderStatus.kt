package org.burufi.monitoring.delivery.model

/**
 * Type of the status of a delivery order.
 */
enum class OrderStatus(val code: String) {
    /**
     * Delivery order is registered.
     */
    REGISTERED("R"),

    /**
     * Delivery order is on its way to the destination.
     */
    SENT("S"),

    /**
     * Order is delivered.
     */
    DELIVERED("D")
}
