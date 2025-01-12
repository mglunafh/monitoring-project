package org.burufi.monitoring.dto.warehouse

import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class ContractItemOrderDto(
    val id: Int,
    @field:Positive("Item price must be positive")
    val price: BigDecimal,
    @field:Positive("Amount of items to order from a supplier must be positive")
    val amount: Int
)
