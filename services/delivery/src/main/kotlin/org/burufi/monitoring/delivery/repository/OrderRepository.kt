package org.burufi.monitoring.delivery.repository

import org.burufi.monitoring.delivery.model.DeliveryOrder
import org.burufi.monitoring.delivery.model.OrderStatus
import org.springframework.data.repository.CrudRepository

@JvmDefaultWithoutCompatibility
interface OrderRepository : CrudRepository<DeliveryOrder, Int> {

    fun findByShoppingCartId(shoppingCartId: String): DeliveryOrder?

    fun findByStatusIn(vararg statuses: OrderStatus): List<DeliveryOrder>

    fun findFirstByTransportTypeMarkAndStatus(mark: String, status: OrderStatus): DeliveryOrder?

    fun findAwaitingOrder(mark: String) = findFirstByTransportTypeMarkAndStatus(mark, OrderStatus.REGISTERED)
}
