package org.burufi.monitoring.delivery.service

import jakarta.transaction.Transactional
import org.burufi.monitoring.delivery.exception.DeliveryException
import org.burufi.monitoring.delivery.exception.FailureType
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

    @Transactional(rollbackOn = [DeliveryException::class])
    fun deliverOrder(orderId: Int, transportId: Int, cost: BigDecimal) {
        log.info("Order {} delivered", orderId)

        val order = orderRepo.findByIdOrNull(orderId)
        if (order == null) throw DeliveryException(FailureType.ORDER_ID_NOT_FOUND)

        val transport = transportRepo.findByIdOrNull(transportId)
        if (transport == null) throw DeliveryException(FailureType.TRANSPORT_ID_NOT_FOUND)

        val orderStatus = order.status
        if (orderStatus != OrderStatus.SENT) throw DeliveryException(FailureType.ORDER_DID_NOT_HAVE_STATUS_SENT)

        val transportStatus = transport.status
        if (transportStatus != TransportStatus.DELIVERING) throw DeliveryException(FailureType.TRANSPORT_DID_NOT_HAVE_STATUS_DELIVERING)

        order.status = OrderStatus.DELIVERED
        order.arrivalTime = LocalDateTime.now()
        order.cost = cost
        transport.status = TransportStatus.RETURNING

        orderRepo.save(order)
        transportRepo.save(transport)
    }

    @Transactional(rollbackOn = [DeliveryException::class])
    fun returnTransport(transportId: Int): OrderCapture? {
        log.info("Transport {} returns back to the park", transportId)

        val transport = transportRepo.findByIdOrNull(transportId)
        if (transport == null) throw DeliveryException(FailureType.TRANSPORT_ID_NOT_FOUND)

        val transportStatus = transport.status
        if (transportStatus != TransportStatus.RETURNING) throw DeliveryException(FailureType.TRANSPORT_DID_NOT_HAVE_STATUS_RETURNING)

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
