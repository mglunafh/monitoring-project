package org.burufi.monitoring.delivery.service

import jakarta.transaction.Transactional
import org.burufi.monitoring.delivery.dto.CreateDeliveryOrderDto
import org.burufi.monitoring.delivery.dto.DeliveryOrderDto
import org.burufi.monitoring.delivery.exception.DeliveryException
import org.burufi.monitoring.delivery.exception.FailureType
import org.burufi.monitoring.delivery.mapper.OrderMapper
import org.burufi.monitoring.delivery.model.DeliveryOrder
import org.burufi.monitoring.delivery.model.OrderStatus
import org.burufi.monitoring.delivery.repository.OrderRepository
import org.burufi.monitoring.delivery.repository.TransportTypeRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class DeliveryOrderService(
    private val orderRepo: OrderRepository,
    private val transportTypeRepo: TransportTypeRepository
) {

    @Transactional(rollbackOn = [DeliveryException::class])
    fun create(createOrder: CreateDeliveryOrderDto): DeliveryOrder {
        val mark = createOrder.transportMark
        val transportType = transportTypeRepo.findByMark(mark)
        if (transportType == null) throw DeliveryException(FailureType.TRANSPORT_MARK_NOT_FOUND)

        val orderWithShoppingCart = orderRepo.findByShoppingCartId(createOrder.shoppingCartId)
        if (orderWithShoppingCart != null) throw DeliveryException(FailureType.SHOPPING_CART_ID_ALREADY_EXISTS)

        val order = DeliveryOrder(
            shoppingCartId = createOrder.shoppingCartId,
            distance = createOrder.distance,
            transportType = transportType,
            orderTime = LocalDateTime.now()
        )
        orderRepo.save(order)

        return order
    }

    @Transactional
    fun getOngoing(): List<DeliveryOrderDto> {
        val ongoingOrders = orderRepo.findByStatus(OrderStatus.REGISTERED)
        return ongoingOrders.map { OrderMapper.map(it) }
    }
}
