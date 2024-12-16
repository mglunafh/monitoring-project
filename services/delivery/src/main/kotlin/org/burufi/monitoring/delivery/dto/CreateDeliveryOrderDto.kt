package org.burufi.monitoring.delivery.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class CreateDeliveryOrderDto(
    @field:NotBlank("Shopping Cart ID must not be blank")
    val shoppingCartId: String,
    @field:NotBlank("Transport mark must not be blank")
    val transportMark: String,
    @field:Positive("Distance must be positive")
    val distance: Int
)
