package org.burufi.monitoring.delivery.integration

import org.assertj.core.api.Assertions.assertThat
import org.burufi.monitoring.delivery.TEST_KIA_RIO
import org.burufi.monitoring.delivery.TEST_SHOPPING_CART
import org.burufi.monitoring.delivery.dto.CreatedDeliveryOrder
import org.burufi.monitoring.delivery.dto.DeliveryResponse
import org.burufi.monitoring.delivery.dto.ListOrderResponse
import org.burufi.monitoring.delivery.dto.ResponseCode
import org.burufi.monitoring.delivery.repository.OrderRepository
import org.burufi.monitoring.delivery.typeRef
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test-h2")
class DeliveryIntegrationTest {

    companion object {
        const val CREATE_URL = "/delivery"
        const val ONGOING_ORDERS_URL = "/delivery/ongoing"

        const val TEST_REQUEST =
            """{"shoppingCartId": "$TEST_SHOPPING_CART", "transportMark": "$TEST_KIA_RIO", "distance": 150 }"""

        val typeRefCreateOrder = typeRef<DeliveryResponse<CreatedDeliveryOrder>>()
        val typeRefOngoingOrders = typeRef<DeliveryResponse<ListOrderResponse>>()
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
    fun `test creation`() {
        val headers = HttpHeaders().apply {
            add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        }
        val createResult = restTemplate.exchange(CREATE_URL, HttpMethod.POST, HttpEntity(TEST_REQUEST, headers), typeRefCreateOrder)
        val responseCreate = requireNotNull(createResult.body)

        assertThat(responseCreate).extracting("responseCode", "errorMessage", "payload.orderId")
            .isEqualTo(listOf(ResponseCode.OK, null, 1))

        val ongoingOrdersResult = restTemplate.exchange(ONGOING_ORDERS_URL, HttpMethod.GET, HttpEntity<Any>(null, null) , typeRefOngoingOrders)
        val responseOrders = requireNotNull(ongoingOrdersResult.body)

        assertThat(responseOrders).extracting("responseCode", "errorMessage").isEqualTo(listOf(ResponseCode.OK, null))
        val ongoingOrdersList = requireNotNull(responseOrders.payload).orders
        assertThat(ongoingOrdersList).hasSize(1)

        val createdDeliveryOrder = requireNotNull(responseCreate.payload)
        assertThat(ongoingOrdersList[0]).extracting("id", "shoppingCartId", "distance", "transportMark", "orderTime")
            .isEqualTo(listOf(createdDeliveryOrder.orderId, TEST_SHOPPING_CART, 150, TEST_KIA_RIO, createdDeliveryOrder.orderTime))
    }
}
