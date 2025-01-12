package org.burufi.monitoring.dto.warehouse

import java.math.BigDecimal

data class ContractItemOrderDto(val itemId: Int, val itemPrice: BigDecimal, val amount: Int)
