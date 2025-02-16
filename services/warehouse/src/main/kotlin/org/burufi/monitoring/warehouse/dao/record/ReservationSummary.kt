package org.burufi.monitoring.warehouse.dao.record

data class ReservationSummary(val shoppingCartId: String, val status: ReserveStatus, val count: Int)
