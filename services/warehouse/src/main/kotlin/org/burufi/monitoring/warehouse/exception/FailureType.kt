package org.burufi.monitoring.warehouse.exception

/**
 * Types of errors which could happen during request processing.
 */
enum class FailureType {
    SUPPLIER_ID_NOT_FOUND,
    PRODUCT_ID_NOT_FOUND,
    RESERVE_TOO_MANY_ITEMS,
    GENERIC_DATABASE_FAILURE
}
