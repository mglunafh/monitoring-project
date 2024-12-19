package org.burufi.monitoring.delivery.exception

/**
 * Types of errors which could occur during the request handling.
 */
enum class FailureType {
    TRANSPORT_MARK_NOT_FOUND,
    SHOPPING_CART_ID_ALREADY_EXISTS
}
