package org.burufi.monitoring.dto.warehouse

import org.burufi.monitoring.dto.Payload
import java.math.BigDecimal
import java.time.LocalDateTime

sealed interface WarehousePayload : Payload

data class ListSuppliers(val suppliers: List<SupplierDto>) : WarehousePayload

data class ListGoods(val goods: List<GoodsItemDto>) : WarehousePayload

data class RegisteredContract(val id: Int, val signDate: LocalDateTime, val totalCost: BigDecimal) : WarehousePayload

data class ContractInfo(val id: Int, val supplier: String, val signDate: LocalDateTime, val totalCost: BigDecimal) : WarehousePayload

data class CancelledReservation(
    val shoppingCartId: String,
    val message: String,
    val cancelTime: LocalDateTime? = null,
    val items: List<ReservationItemDto> = listOf()
) : WarehousePayload

data class PurchasedReservation(
    val shoppingCartId: String,
    val message: String,
    val purchaseTime: LocalDateTime? = null,
    val items: List<ReservationItemDto> = listOf()
) : WarehousePayload
