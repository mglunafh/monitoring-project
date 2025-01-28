package org.burufi.monitoring.dto.warehouse

import java.time.LocalDateTime

data class ReservationItemInfoDto(
    val id: Int,
    val amount: Int,
    val firstModified: LocalDateTime,
    val lastModified: LocalDateTime
)
