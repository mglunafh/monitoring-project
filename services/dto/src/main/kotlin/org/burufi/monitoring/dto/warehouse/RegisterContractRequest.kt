package org.burufi.monitoring.dto.warehouse

import java.math.BigDecimal

data class RegisterContractRequest(val supplierId: Int, val items: List<ContractItemOrderDto>) {

    val totalPrice: BigDecimal
        get() = items.sumOf { it.itemPrice }
}
