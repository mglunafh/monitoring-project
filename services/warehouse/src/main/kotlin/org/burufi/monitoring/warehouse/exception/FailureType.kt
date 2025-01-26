package org.burufi.monitoring.warehouse.exception

/**
 * Types of errors which could happen during request processing.
 */
sealed interface FailureType {

    object SupplierIdNotFound : FailureType

    data class ProductIdNotFound(val message: String?) : FailureType

    object ReserveTooManyItems : FailureType

    data class GenericDatabaseFailure(val cause: Throwable) : FailureType
}
