package org.burufi.monitoring.shop.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CustomerDto(val id: Int, val login: String, val registeredAt: LocalDateTime)
