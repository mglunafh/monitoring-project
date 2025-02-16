package org.burufi.monitoring.dto.delivery

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.burufi.monitoring.dto.ShoppingCartId

data class CreateDeliveryOrderRequest(
    @field:ShoppingCartId
    val shoppingCartId: String,
    @field:NotBlank("Transport mark must not be blank")
    val transportMark: String,
    @field:Positive("Distance must be positive")
    val distance: Int
)
