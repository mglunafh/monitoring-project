package org.burufi.monitoring.dto.warehouse

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class ReserveItemRequest(
    @field:NotBlank("ShoppingCart ID must not be blank")
    val shoppingCartId: String,
    val itemId: Int,
    @field:Positive("Amount of items to reserve must be positive")
    val amount: Int
)
