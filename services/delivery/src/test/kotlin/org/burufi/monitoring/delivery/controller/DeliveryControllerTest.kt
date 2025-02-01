package org.burufi.monitoring.delivery.controller

import org.burufi.monitoring.delivery.GAZELLE_MARK
import org.burufi.monitoring.delivery.ORDER_TIME_AS_STRING
import org.burufi.monitoring.delivery.TEST_CREATE_ORDER_DTO
import org.burufi.monitoring.delivery.TEST_CREATE_ORDER_REQUEST
import org.burufi.monitoring.delivery.TEST_DELIVERY_ORDER_DTO
import org.burufi.monitoring.delivery.TEST_ORDER
import org.burufi.monitoring.delivery.TEST_SHOPPING_CART
import org.burufi.monitoring.delivery.config.ObjectMapperConfig
import org.burufi.monitoring.delivery.exception.DeliveryException
import org.burufi.monitoring.delivery.exception.FailureType
import org.burufi.monitoring.delivery.exception.FailureType.SHOPPING_CART_ID_ALREADY_EXISTS
import org.burufi.monitoring.delivery.service.DeliveryOrderService
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import kotlin.test.Test

@WebMvcTest(controllers = [DeliveryController::class])
@Import(ObjectMapperConfig::class)
class DeliveryControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var orderService: DeliveryOrderService

    @Test
    fun `test create order`() {
        Mockito.doReturn(TEST_ORDER.copy(id = 666)).`when`(orderService).create(TEST_CREATE_ORDER_DTO)

        mockMvc.perform(MockMvcRequestBuilders.post("/delivery")
            .content(TEST_CREATE_ORDER_REQUEST)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("OK"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.shoppingCartId").value(TEST_SHOPPING_CART))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.orderId").value(666))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.orderTime").value(ORDER_TIME_AS_STRING))
    }

    @Test
    fun `test create order, validation error`() {
        val badRequest = """
            {"shoppingCartId": "", "transportMark": "$GAZELLE_MARK", "distance": 150 }
        """.trimIndent()

        mockMvc.perform(MockMvcRequestBuilders.post("/delivery")
            .content(badRequest)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("VALIDATION_FAILURE"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value("Invalid shopping cart ID format"))
    }

    @Test
    fun `test create order, malformed shopping cart ID`() {
        val badRequest = """
            {"shoppingCartId": "test-shopping-cart_id", "transportMark": "$GAZELLE_MARK", "distance": 150 }
        """.trimIndent()

        mockMvc.perform(MockMvcRequestBuilders.post("/delivery")
            .content(badRequest)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("VALIDATION_FAILURE"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value("Invalid shopping cart ID format"))
    }

    @Test
    fun `test create order, the shopping cart is already registered`() {
        Mockito.doThrow(DeliveryException(SHOPPING_CART_ID_ALREADY_EXISTS)).`when`(orderService).create(TEST_CREATE_ORDER_DTO)

        mockMvc.perform(MockMvcRequestBuilders.post("/delivery")
            .content(TEST_CREATE_ORDER_REQUEST)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("VALIDATION_FAILURE"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value(DeliveryExceptionHandler.SHOPPING_CARD_ALREADY_REGISTERED))
    }

    @Test
    fun `test create order, no such transport`() {
        Mockito.doThrow(DeliveryException(FailureType.TRANSPORT_MARK_NOT_FOUND)).`when`(orderService).create(TEST_CREATE_ORDER_DTO)

        mockMvc.perform(MockMvcRequestBuilders.post("/delivery")
            .content(TEST_CREATE_ORDER_REQUEST)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("VALIDATION_FAILURE"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value(DeliveryExceptionHandler.INCORRECT_TRANSPORT_MARK))
    }

    @Test
    fun `test ongoing orders`() {
        Mockito.doReturn(listOf(TEST_DELIVERY_ORDER_DTO)).`when`(orderService).getOngoing()

        mockMvc.perform(MockMvcRequestBuilders.get("/delivery/ongoing")
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("OK"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.orders[0].id").value(1349))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.orders[0].shoppingCartId").value(TEST_SHOPPING_CART))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.orders[0].distance").value(100))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.orders[0].orderTime").value(ORDER_TIME_AS_STRING))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.orders[1]").doesNotExist())
    }
}
