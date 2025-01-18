package org.burufi.monitoring.warehouse.controller

import org.burufi.monitoring.dto.warehouse.GoodsItemDto
import org.burufi.monitoring.dto.warehouse.RegisteredContract
import org.burufi.monitoring.dto.warehouse.SupplierDto
import org.burufi.monitoring.warehouse.service.WarehouseService
import org.hamcrest.core.StringContains
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal
import kotlin.test.Test

@WebMvcTest(controllers = [WarehouseController::class])
class WarehouseControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var warehouseService: WarehouseService

    @Test
    fun `Test list suppliers`() {
        val testSuppliers = listOf(SupplierDto(id = 100, name = "TestSupplier", description = "Test Description"))
        Mockito.doReturn(testSuppliers).`when`(warehouseService).getSuppliers()

        mockMvc.perform(MockMvcRequestBuilders.get("/warehouse/suppliers").accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("OK"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.suppliers[0].id").value(100))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.suppliers[0].name").value("TestSupplier"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.suppliers[0].description").value("Test Description"))
    }

    @Test
    fun `Test list goods`() {
        val item = GoodsItemDto(10, "Test instance", "TEST", 1, BigDecimal(180.5))
        val testGoods = listOf(item)
        Mockito.doReturn(testGoods).`when`(warehouseService).getGoods()

        mockMvc.perform(MockMvcRequestBuilders.get("/warehouse/goods").accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("OK"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.goods[0].id").value(item.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.goods[0].name").value(item.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.goods[0].category").value(item.category))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.goods[0].amount").value(item.amount))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.goods[0].weight").value(item.weight))
    }

    @Test
    fun `Test register contract`() {
        val request = """
            {"suppliedId": 100, "items": [
                { "id": 6, "price": 10, "amount": 5 }
            ]}
        """.trimIndent()

        Mockito.doReturn(RegisteredContract(id = 15)).`when`(warehouseService).registerContract(any())

        mockMvc.perform(MockMvcRequestBuilders.post("/warehouse/contract")
            .content(request)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("OK"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.id").value(15))
    }

    @Test
    fun `Test register contract, but no items in the request`() {
        val badRequest = """
            {"suppliedId": 100, "items": []}
        """.trimIndent()

        mockMvc.perform(MockMvcRequestBuilders.post("/warehouse/contract")
            .content(badRequest)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("VALIDATION_FAILURE"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage")
                .value("Field 'items': Contract must contain some items, got '[]' instead."))
    }

    @Test
    fun `Test register contract, but some item is incorrect`() {
        val badRequest = """
            { "suppliedId": 100, "items": [ { "id": 15, "price": -10, "amount": -2 } ] }
        """.trimIndent()

        mockMvc.perform(MockMvcRequestBuilders.post("/warehouse/contract")
            .content(badRequest)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("VALIDATION_FAILURE"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage")
                .value(StringContains.containsString("Field 'items[0].amount': Amount of items to order from a supplier must be positive, got '-2' instead.")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage")
                .value(StringContains.containsString("Field 'items[0].price': Item price must be positive, got '-10' instead.")))
    }
}
