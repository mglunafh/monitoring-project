package org.burufi.monitoring.delivery.repository

import org.burufi.monitoring.delivery.model.DeliveryOrder
import org.burufi.monitoring.delivery.model.OrderStatistics
import org.burufi.monitoring.delivery.model.OrderStatus
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

@JvmDefaultWithoutCompatibility
interface OrderRepository : CrudRepository<DeliveryOrder, Int> {

    fun findByShoppingCartId(shoppingCartId: String): DeliveryOrder?

    fun findByStatusIn(vararg statuses: OrderStatus): List<DeliveryOrder>

    fun findFirstByTransportTypeMarkAndStatus(mark: String, status: OrderStatus): DeliveryOrder?

    fun findAwaitingOrder(mark: String) = findFirstByTransportTypeMarkAndStatus(mark, OrderStatus.REGISTERED)

    @Query(value = """
        SELECT do.status as status, count(do.id) as orderCount, sum(do.cost) as totalCost 
            FROM DeliveryOrder do GROUP BY do.status
    """)
    fun findOrderStatistics(): List<OrderStatistics>

}
