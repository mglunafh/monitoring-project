package org.burufi.monitoring.dto.warehouse

import org.burufi.monitoring.dto.ShoppingCartId

data class ProcessReserveRequest(
    @field:ShoppingCartId
    val shoppingCartId: String
)
