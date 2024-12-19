package org.burufi.monitoring.delivery.model

/**
 * Status of the transport.
 */
enum class TransportStatus(val code: String) {

    /**
     * Transport unit is in the park and ready to deliver the orders.
     */
    AVAILABLE("AV"),

    /**
     * Transport unit is on the way to deliver an order.
     */
    DELIVERING("D"),

    /**
     * Transport unit is on its way back to the park.
     */
    RETURNING("RET")
}
