package org.burufi.monitoring.warehouse.dao.record

import java.time.LocalDateTime

data class ReservationFullDetails(
    val itemId: Int,
    val amount: Amount,
    val firstModified: LocalDateTime,
    val lastModified: LocalDateTime,
    val status: ReserveStatus
)
