package org.burufi.monitoring.delivery.service

import jakarta.transaction.Transactional
import org.burufi.monitoring.delivery.exception.DeliveryException
import org.burufi.monitoring.delivery.exception.FailureType
import org.burufi.monitoring.delivery.mapper.OrderMapper
import org.burufi.monitoring.delivery.model.DeliveryOrder
import org.burufi.monitoring.delivery.model.OrderStatistics
import org.burufi.monitoring.delivery.model.OrderStatus
import org.burufi.monitoring.delivery.model.TransportStatus
import org.burufi.monitoring.delivery.repository.OrderRepository
import org.burufi.monitoring.delivery.repository.TransportRepository
import org.burufi.monitoring.delivery.repository.TransportTypeRepository
import org.burufi.monitoring.dto.delivery.CreateDeliveryOrderRequest
import org.burufi.monitoring.dto.delivery.DeliveryOrderDto
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class DeliveryOrderService(
    private val orderRepo: OrderRepository,
    private val transportTypeRepo: TransportTypeRepository,
    private val transportRepo: TransportRepository,
    private val backgroundManager: BackgroundOrderManager
) {

    @Transactional(rollbackOn = [DeliveryException::class])
    fun create(createOrder: CreateDeliveryOrderRequest): DeliveryOrder {
        val mark = createOrder.transportMark
        val transportType = transportTypeRepo.findByMark(mark)
        if (transportType == null) throw DeliveryException(FailureType.TRANSPORT_MARK_NOT_FOUND)

        val orderWithShoppingCart = orderRepo.findByShoppingCartId(createOrder.shoppingCartId)
        if (orderWithShoppingCart != null) throw DeliveryException(FailureType.SHOPPING_CART_ID_ALREADY_EXISTS)

        val order = DeliveryOrder(
            shoppingCartId = createOrder.shoppingCartId,
            distance = createOrder.distance,
            transportType = transportType,
            orderTime = LocalDateTime.now(),
        )

        val availableTransport = transportRepo.findFirstByTransportTypeMarkAndStatus(mark)
        if (availableTransport != null) {
            availableTransport.status = TransportStatus.DELIVERING
            order.transport = availableTransport
            order.status = OrderStatus.SENT
            order.departureTime = LocalDateTime.now()
        }

        val createdOrder = orderRepo.save(order)
        backgroundManager.sendOrder(createdOrder)

        return createdOrder
    }

    @Transactional
    fun getOngoing(): List<DeliveryOrderDto> {
        val ongoingOrders = orderRepo.findByStatusIn(OrderStatus.REGISTERED, OrderStatus.SENT)
        return ongoingOrders.map { OrderMapper.map(it) }
    }

    @Transactional
    fun getStatistics(): List<OrderStatistics> {
        return orderRepo.findOrderStatistics()
    }
}
