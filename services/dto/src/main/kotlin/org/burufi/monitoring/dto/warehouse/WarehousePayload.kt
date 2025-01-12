package org.burufi.monitoring.dto.warehouse

import org.burufi.monitoring.dto.Payload

sealed interface WarehousePayload : Payload

data class ListSuppliers(val suppliers: List<SupplierDto>) : WarehousePayload

data class ListGoods(val goods: List<GoodsItemDto>) : WarehousePayload

data class RegisteredContract(val some: Any) : WarehousePayload
