package org.burufi.monitoring.dto.warehouse

import org.burufi.monitoring.dto.Payload
import java.math.BigDecimal
import java.time.LocalDateTime

sealed interface WarehousePayload : Payload

data class ListSuppliers(val suppliers: List<SupplierDto>) : WarehousePayload

data class ListGoods(val goods: List<GoodsItemDto>) : WarehousePayload

data class RegisteredContract(val id: Int, val signDate: LocalDateTime, val totalCost: BigDecimal) : WarehousePayload
