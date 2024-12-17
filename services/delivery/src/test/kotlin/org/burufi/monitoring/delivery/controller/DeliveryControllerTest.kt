package org.burufi.monitoring.delivery.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.burufi.monitoring.delivery.GAZELLE_MARK
import org.burufi.monitoring.delivery.TEST_CREATE_ORDER_DTO
import org.burufi.monitoring.delivery.TEST_ORDER
import org.burufi.monitoring.delivery.dto.CreateDeliveryOrderDto
import org.burufi.monitoring.delivery.exception.DeliveryException
import org.burufi.monitoring.delivery.exception.FailureType
import org.burufi.monitoring.delivery.exception.FailureType.SHOPPING_CART_ID_ALREADY_EXISTS
import org.burufi.monitoring.delivery.service.DeliveryOrderService
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import kotlin.test.Test

@WebMvcTest(DeliveryController::class)
class DeliveryControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var orderService: DeliveryOrderService

    @Test
    fun `test create order`() {
        Mockito.doReturn(TEST_ORDER.copy(id = 666)).`when`(orderService).create(TEST_CREATE_ORDER_DTO)

        mockMvc.perform(MockMvcRequestBuilders.post("/delivery")
            .content(objectMapper.writeValueAsString(TEST_CREATE_ORDER_DTO))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("OK"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.orderId").value(666))
            .andExpect(MockMvcResultMatchers.jsonPath("$.orderTime").value("2020-01-01 10:30:00.000"))
    }

    @Test
    fun `test create order, validation error`() {
        val createOrderDto = CreateDeliveryOrderDto("", GAZELLE_MARK, 150)

        mockMvc.perform(MockMvcRequestBuilders.post("/delivery")
            .content(objectMapper.writeValueAsString(createOrderDto))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("VALIDATION_FAILURE"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value("Shopping Cart ID must not be blank"))
    }

    @Test
    fun `test create order, the shopping cart is already registered`() {
        Mockito.doThrow(DeliveryException(SHOPPING_CART_ID_ALREADY_EXISTS)).`when`(orderService).create(TEST_CREATE_ORDER_DTO)

        mockMvc.perform(MockMvcRequestBuilders.post("/delivery")
            .content(objectMapper.writeValueAsString(TEST_CREATE_ORDER_DTO))
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
            .content(objectMapper.writeValueAsString(TEST_CREATE_ORDER_DTO))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("VALIDATION_FAILURE"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value(DeliveryExceptionHandler.INCORRECT_TRANSPORT_MARK))
    }
}
