package org.burufi.monitoring.delivery.service

import jakarta.transaction.Transactional
import org.burufi.monitoring.delivery.exception.BackgroundFailureType.OrderIdNotFound
import org.burufi.monitoring.delivery.exception.BackgroundFailureType.TransportIdNotFound
import org.burufi.monitoring.delivery.exception.BackgroundFailureType.UnexpectedOrderStatus
import org.burufi.monitoring.delivery.exception.BackgroundFailureType.UnexpectedTransportStatus
import org.burufi.monitoring.delivery.exception.DeliveryBackgroundException
import org.burufi.monitoring.delivery.model.OrderStatus
import org.burufi.monitoring.delivery.model.TransportStatus
import org.burufi.monitoring.delivery.repository.OrderRepository
import org.burufi.monitoring.delivery.repository.TransportRepository
import org.burufi.monitoring.delivery.service.OrderCapture.Companion.toCapture
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class BackgroundOrderService(
    private val orderRepo: OrderRepository,
    private val transportRepo: TransportRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional(rollbackOn = [DeliveryBackgroundException::class])
    fun deliverOrder(orderId: Int, transportId: Int, cost: BigDecimal) {
        log.info("Order {} delivered, total expenses: {}", orderId, cost)

        val order = orderRepo.findByIdOrNull(orderId)
        if (order == null) throw OrderIdNotFound(orderId).asException()

        val transport = transportRepo.findByIdOrNull(transportId)
        if (transport == null) throw TransportIdNotFound(transportId).asException()

        val orderStatus = order.status
        if (orderStatus != OrderStatus.SENT) throw UnexpectedOrderStatus(OrderStatus.SENT, orderStatus).asException()

        val transportStatus = transport.status
        if (transportStatus != TransportStatus.DELIVERING) {
            throw UnexpectedTransportStatus(TransportStatus.DELIVERING, transportStatus).asException()
        }

        order.status = OrderStatus.DELIVERED
        order.arrivalTime = LocalDateTime.now()
        order.cost = cost
        transport.status = TransportStatus.RETURNING

        orderRepo.save(order)
        transportRepo.save(transport)
    }

    @Transactional(rollbackOn = [DeliveryBackgroundException::class])
    fun returnTransport(transportId: Int): OrderCapture? {
        log.info("Transport {} returns back to the park", transportId)

        val transport = transportRepo.findByIdOrNull(transportId)
        if (transport == null) throw DeliveryBackgroundException(TransportIdNotFound(transportId))

        val transportStatus = transport.status
        if (transportStatus != TransportStatus.RETURNING) {
            throw DeliveryBackgroundException(UnexpectedTransportStatus(TransportStatus.RETURNING, transportStatus))
        }

        val awaiting = orderRepo.findAwaitingOrder(transport.transportType.mark)
        return if (awaiting == null) {
            log.info("Transport {} is marked available", transportId)
            transport.status = TransportStatus.AVAILABLE
            transportRepo.save(transport)
            null
        } else {
            log.info("Transport {} is picking up order {}", transportId, awaiting.id)
            transport.status = TransportStatus.DELIVERING
            transportRepo.save(transport)

            awaiting.transport = transport
            awaiting.status = OrderStatus.SENT
            awaiting.departureTime = LocalDateTime.now()
            val savedOrder = orderRepo.save(awaiting)

            savedOrder.toCapture()
        }
    }
}
