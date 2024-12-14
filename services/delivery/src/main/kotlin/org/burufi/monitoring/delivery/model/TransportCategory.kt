package org.burufi.monitoring.delivery.model

/**
 * Various categories of transport an order can be delivered by.
 */
enum class TransportCategory {

    /**
     * Light passenger car. Suitable for the most small and medium size orders.
     */
    LIGHT_CAR,

    /**
     * Big truck. Suitable for big size orders.
     */
    TRUCK,

    /**
     * Nimble aerial transport. Suitable for quick delivery of the small orders.
     */
    QUADCOPTER,

    /**
     * Cargo helicopter. Suitable for delivering the medium and large size orders with unmatched speed.
     */
    HELICOPTER
}
