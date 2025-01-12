package org.burufi.monitoring.dto.warehouse

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import java.math.BigDecimal

data class RegisterContractRequest(
    val supplierId: Int,
    @field:Valid @field:NotEmpty("Contract must contain some items")
    val items: List<ContractItemOrderDto>
) {

    val totalPrice: BigDecimal
        get() = items.sumOf { it.price }
}
