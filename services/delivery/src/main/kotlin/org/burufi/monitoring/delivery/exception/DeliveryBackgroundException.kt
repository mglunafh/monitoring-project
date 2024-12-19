package org.burufi.monitoring.delivery.exception

class DeliveryBackgroundException(failure: BackgroundFailureType) : RuntimeException() {

    override val message: String = when(failure) {
        is BackgroundFailureType.OrderIdNotFound -> "Somehow, order with id '${failure.orderId}' was not found."
        is BackgroundFailureType.TransportIdNotFound ->
            "Somehow, transport with id '${failure.transportId}' was not found."
        is BackgroundFailureType.UnexpectedOrderStatus ->
            "Encountered an incorrect order status '${failure.actual}': expected '${failure.expected}'."
        is BackgroundFailureType.UnexpectedTransportStatus ->
            "Encountered an incorrect transport status '${failure.actual}': expected '${failure.expected}'."
    }
}
