package org.burufi.monitoring.shop.model

import java.time.LocalDateTime

data class Customer(val id: Int, val login: String, val registeredAt: LocalDateTime, val password: String)
