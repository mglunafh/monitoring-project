package org.burufi.monitoring.delivery.repository

import org.burufi.monitoring.delivery.model.DeliveryOrder
import org.burufi.monitoring.delivery.model.OrderStatus
import org.springframework.data.repository.CrudRepository

interface OrderRepository : CrudRepository<DeliveryOrder, Int> {

    fun findByShoppingCartId(shoppingCartId: String): DeliveryOrder?

    fun findByStatus(status: OrderStatus): List<DeliveryOrder>

}
