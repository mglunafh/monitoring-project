package org.burufi.monitoring.delivery.integration

import org.assertj.core.api.Assertions.assertThat
import org.burufi.monitoring.delivery.TEST_KIA_RIO
import org.burufi.monitoring.delivery.TEST_SHOPPING_CART
import org.burufi.monitoring.delivery.model.OrderStatus
import org.burufi.monitoring.delivery.repository.OrderRepository
import org.burufi.monitoring.delivery.typeRef
import org.burufi.monitoring.dto.MyResponse
import org.burufi.monitoring.dto.ResponseCode
import org.burufi.monitoring.dto.delivery.CreatedDeliveryOrder
import org.burufi.monitoring.dto.delivery.ListOrders
import org.burufi.monitoring.dto.delivery.OrderStatisticsDto
import org.burufi.monitoring.dto.delivery.OrderStatistics
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import kotlin.test.Test

abstract class DeliveryIntegrationTest {

    companion object {
        const val CREATE_URL = "/delivery"
        const val ONGOING_ORDERS_URL = "/delivery/ongoing"
        const val STATS_URL = "/delivery/stats"

        const val TEST_REQUEST =
            """{"shoppingCartId": "$TEST_SHOPPING_CART", "transportMark": "$TEST_KIA_RIO", "distance": 150 }"""

        val typeRefCreateOrder = typeRef<MyResponse<CreatedDeliveryOrder>>()
        val typeRefOngoingOrders = typeRef<MyResponse<ListOrders>>()
        val typeRefStats = typeRef<MyResponse<OrderStatistics>>()
    }

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var orderRepo: OrderRepository

    @BeforeEach
    fun cleanUp() {
        orderRepo.deleteAll()
    }

    @Test
    fun `Create an order, check if it's present in ongoing orders and shows up in statistics`() {
        val headers = HttpHeaders().apply {
            add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        }
        val createResult = restTemplate.exchange(CREATE_URL, HttpMethod.POST, HttpEntity(TEST_REQUEST, headers), typeRefCreateOrder)
        val responseCreate = requireNotNull(createResult.body)

        assertThat(responseCreate).extracting("responseCode", "errorMessage", "payload.shoppingCartId", "payload.orderId")
            .isEqualTo(listOf(ResponseCode.OK, null, TEST_SHOPPING_CART, 1))

        val ongoingOrdersResult = restTemplate.exchange(ONGOING_ORDERS_URL, HttpMethod.GET, HttpEntity<Any>(null, null) , typeRefOngoingOrders)
        val responseOrders = requireNotNull(ongoingOrdersResult.body)

        assertThat(responseOrders).extracting("responseCode", "errorMessage").isEqualTo(listOf(ResponseCode.OK, null))
        val ongoingOrdersList = requireNotNull(responseOrders.payload).orders
        assertThat(ongoingOrdersList).hasSize(1)

        val createdDeliveryOrder = requireNotNull(responseCreate.payload)
        assertThat(ongoingOrdersList[0]).extracting("id", "shoppingCartId", "distance", "transportMark", "orderTime")
            .isEqualTo(listOf(createdDeliveryOrder.orderId, TEST_SHOPPING_CART, 150, TEST_KIA_RIO, createdDeliveryOrder.orderTime))

        val statsResult = restTemplate.exchange(STATS_URL, HttpMethod.GET, HttpEntity<Any>(null, null), typeRefStats)
        val responseStats = requireNotNull(statsResult.body)

        assertThat(responseStats).extracting("responseCode", "errorMessage").isEqualTo(listOf(ResponseCode.OK, null))
        val stats = requireNotNull(responseStats.payload).statistics
        assertThat(stats[0]).isEqualTo(OrderStatisticsDto(OrderStatus.REGISTERED.name, 1, null))
    }
}
