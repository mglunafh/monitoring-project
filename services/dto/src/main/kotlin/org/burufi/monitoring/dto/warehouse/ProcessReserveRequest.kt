package org.burufi.monitoring.dto.warehouse

import jakarta.validation.constraints.NotBlank

data class ProcessReserveRequest(
    @field:NotBlank("ShoppingCart ID must not be blank")
    val shoppingCartId: String
)
