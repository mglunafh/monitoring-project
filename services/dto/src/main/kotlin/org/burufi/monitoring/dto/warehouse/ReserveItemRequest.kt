package org.burufi.monitoring.dto.warehouse

import jakarta.validation.constraints.Positive
import org.burufi.monitoring.dto.ShoppingCartId

data class ReserveItemRequest(
    @field:ShoppingCartId
    val shoppingCartId: String,
    val itemId: Int,
    @field:Positive("Amount of items to reserve must be positive")
    val amount: Int
)
