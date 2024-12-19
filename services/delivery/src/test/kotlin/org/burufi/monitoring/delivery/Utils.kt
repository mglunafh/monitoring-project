package org.burufi.monitoring.delivery

import org.burufi.monitoring.delivery.dto.CreateDeliveryOrderDto
import org.burufi.monitoring.delivery.mapper.OrderMapper
import org.burufi.monitoring.delivery.model.DeliveryOrder
import org.burufi.monitoring.delivery.model.TransportCategory.QUADCOPTER
import org.burufi.monitoring.delivery.model.TransportCategory.TRUCK
import org.burufi.monitoring.delivery.model.TransportType
import java.math.BigDecimal
import java.time.LocalDateTime

const val TEST_MARK = "Test Mark"
const val GAZELLE_MARK = "Test Gazelle"
const val GEORADAR_MARK = "Test GeoRadar"
val ORDER_TIME: LocalDateTime = LocalDateTime.of(2020, 1, 1, 10, 30, 0)
const val ORDER_TIME_AS_STRING = "2020-01-01 10:30:00.000"

const val TEST_SHOPPING_CART = "first-shopping-cart"

// N.B.: View these entity objects as prototypes.
// Please, take a copy of it before interacting with a persistence context in any way.
val TEST_TRUCK = TransportType(
    category = TRUCK,
    mark = TEST_MARK,
    maxCargo = 1111,
    maxDistance = 2222,
    speed = 60,
    pricePerDistance = BigDecimal(100)
)

val TEST_GAZELLE = TEST_TRUCK.copy(
    mark = GAZELLE_MARK,
    maxCargo = 500,
    maxDistance = 500
)

val TEST_QUADCOPTER = TransportType(
    category = QUADCOPTER,
    mark = GEORADAR_MARK,
    maxCargo = 5,
    maxDistance = 100,
    speed = 15,
    pricePerDistance = BigDecimal(16)
)

val TEST_ORDER = DeliveryOrder(
    shoppingCartId = TEST_SHOPPING_CART,
    distance = 100,
    transportType = TEST_GAZELLE,
    orderTime = ORDER_TIME
)

val TEST_CREATE_ORDER_DTO = CreateDeliveryOrderDto(
    shoppingCartId = TEST_SHOPPING_CART,
    transportMark = GAZELLE_MARK,
    distance = 150
)

val TEST_CREATE_ORDER_REQUEST = """
            {"shoppingCartId": "$TEST_SHOPPING_CART", "transportMark": "$GAZELLE_MARK", "distance": 150 }
        """.trimIndent()

val TEST_DELIVERY_ORDER_DTO = OrderMapper.map(TEST_ORDER.copy(id = 1349))
