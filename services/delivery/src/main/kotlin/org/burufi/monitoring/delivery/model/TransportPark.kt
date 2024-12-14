package org.burufi.monitoring.delivery.model

/**
 * A set of transport vehicles owned by delivery service.
 *
 * @property transportTypeId Transport type.
 * @property status The current status of the transport unit.
 *
 * @see TransportType
 * @see TransportStatus
 */
data class TransportPark(
    val id: Int,
    val transportTypeId: Int,
    var status: TransportStatus
)
