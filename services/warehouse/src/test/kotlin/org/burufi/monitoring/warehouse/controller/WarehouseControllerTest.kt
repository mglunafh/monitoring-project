package org.burufi.monitoring.warehouse.controller

import org.burufi.monitoring.dto.warehouse.CancelledReservation
import org.burufi.monitoring.dto.warehouse.ReservationItemDto
import org.burufi.monitoring.dto.warehouse.ContractInfo
import org.burufi.monitoring.dto.warehouse.GoodsItemDto
import org.burufi.monitoring.dto.warehouse.PurchasedReservation
import org.burufi.monitoring.dto.warehouse.RegisteredContract
import org.burufi.monitoring.dto.warehouse.ReservationInfo
import org.burufi.monitoring.dto.warehouse.ReservationItemInfoDto
import org.burufi.monitoring.dto.warehouse.SupplierDto
import org.burufi.monitoring.warehouse.exception.FailureType
import org.burufi.monitoring.warehouse.exception.WarehouseException
import org.burufi.monitoring.warehouse.service.WarehouseService
import org.hamcrest.core.StringContains
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal
import java.time.LocalDateTime
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
        val testResult = RegisteredContract(id = 15, LocalDateTime.of(2020, 1, 1, 12, 0), BigDecimal.valueOf(50))

        Mockito.doReturn(testResult).`when`(warehouseService).registerContract(any())

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

    @Test
    fun `Test register contract, but item ID is not in the database goods list`() {
        val badRequest = """
            { "suppliedId": 100, "items": [ { "id": 15, "price": 10, "amount": 2 } ] }
        """.trimIndent()

        doThrow(WarehouseException(FailureType.ProductIdNotFound("Item 15 not found.")))
            .`when`(warehouseService).registerContract(any())

        mockMvc.perform(MockMvcRequestBuilders.post("/warehouse/contract")
            .content(badRequest)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("VALIDATION_FAILURE"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage")
                .value(StringContains.containsString("Item 15 not found.")))
    }

    @Test
    fun `Test contract info`() {
        val result = ContractInfo(15, "Test Supplier", LocalDateTime.of(2020, 1, 1, 12, 0), BigDecimal(550))
        doReturn(result).`when`(warehouseService).getContractInfo(15)

        mockMvc.perform(MockMvcRequestBuilders.get("/warehouse/contract/15")
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("OK"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.id").value(15))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.signDate").value("2020-01-01T12:00:00"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.supplier").value("Test Supplier"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.totalCost").value(550))
    }

    @Test
    fun `Test contract info on non-existent contract`() {
        doReturn(null).`when`(warehouseService).getContractInfo(anyInt())

        mockMvc.perform(MockMvcRequestBuilders.get("/warehouse/contract/15")
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("NOT_FOUND"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value("Contract with id '15' is not found"))
    }

    @Test
    fun `Test reserve for empty shopping cart ID`() {
        val badRequest = """{ "shoppingCartId": " ", "itemId": 10, "amount": 10 }"""

        mockMvc.perform(MockMvcRequestBuilders.post("/warehouse/reserve")
            .content(badRequest)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("VALIDATION_FAILURE"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value(
                StringContains.containsString("Field 'shoppingCartId': Invalid shopping cart ID format, got ' ' instead")
            ))
    }

    @Test
    fun `Test reserve for a malformed shopping cart ID`() {
        val badRequest = """{ "shoppingCartId": "test-shopping-cart_1", "itemId": 10, "amount": 10 }"""

        mockMvc.perform(MockMvcRequestBuilders.post("/warehouse/reserve")
            .content(badRequest)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("VALIDATION_FAILURE"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value(
                StringContains.containsString("Invalid shopping cart ID format")
            ))
    }

    @Test
    fun `Test reserve a negative amount`() {
        val badRequest = """{ "shoppingCartId": "test-shopping-cart-1", "itemId": 10, "amount": -10 }"""

        mockMvc.perform(MockMvcRequestBuilders.post("/warehouse/reserve")
            .content(badRequest)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("VALIDATION_FAILURE"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value(
                StringContains.containsString("Field 'amount': Amount of items to reserve must be positive, got '-10' instead")))
    }

    @Test
    fun `Test reserve item`() {
        val request = """{ "shoppingCartId": "test-shopping-cart-1", "itemId": 15, "amount": 10 }"""
        doReturn(true).`when`(warehouseService).reserve(any())

        mockMvc.perform(MockMvcRequestBuilders.post("/warehouse/reserve")
            .content(request)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("OK"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload").isEmpty)
    }

    @Test
    fun `Test reserve non-existent item`() {
        val itemId = 15
        val badRequest = """{ "shoppingCartId": "test-shopping-cart-1", "itemId": $itemId, "amount": 10 }"""

        doReturn(false).`when`(warehouseService).reserve(any())

        mockMvc.perform(MockMvcRequestBuilders.post("/warehouse/reserve")
            .content(badRequest)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("VALIDATION_FAILURE"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value(
                StringContains.containsString("Item with id '$itemId' does not exist")))
    }

    @Test
    fun `Test reserve too much stuff`() {
        val request = """{ "shoppingCartId": "test-shopping-cart-1", "itemId": 15, "amount": 10 }"""
        doThrow(WarehouseException(FailureType.ReserveTooManyItems)).`when`(warehouseService).reserve(any())

        mockMvc.perform(MockMvcRequestBuilders.post("/warehouse/reserve")
            .content(request)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("INTERNAL_SERVER_ERROR"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value(WarehouseExceptionHandler.TOO_FEW_ITEMS_CURRENTLY))
    }

    @Test
    fun `Test retrieve non-existent reservation info`() {
        val shoppingCart = "test-shopping-cart"
        doReturn(null).`when`(warehouseService).getReservationInfo(shoppingCart)

        mockMvc.perform(MockMvcRequestBuilders.get("/warehouse/reserve/$shoppingCart")
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("NOT_FOUND"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value(
                StringContains.containsString("Shopping cart with ID '$shoppingCart' is not found")))
    }

    @Test
    fun `Test reservation info on malformed ID`() {
        val shoppingCart = "test-shopping-cart_1"
        doReturn(null).`when`(warehouseService).getReservationInfo(shoppingCart)

        mockMvc.perform(MockMvcRequestBuilders.get("/warehouse/reserve/$shoppingCart")
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("VALIDATION_FAILURE"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value(
                StringContains.containsString("Invalid shopping cart ID format")))
    }

    @Test
    fun `Test reservation info`() {
        val shoppingCart = "test-shopping-cart"
        val firstReserved   = LocalDateTime.of(2020, 1, 10, 10, 30, 0)
        val lastReserved    = LocalDateTime.of(2020, 1, 10, 10, 31, 0)
        val paid            = LocalDateTime.of(2020, 1, 10, 10, 32, 0)
        val firstModified = "2020-01-10T10:30:00"
        val lastModified = "2020-01-10T10:31:00"

        val reservationInfo = ReservationInfo(
            shoppingCartId = shoppingCart,
            status = "PAID",
            firstReserved = firstReserved,
            lastReserved = lastReserved,
            cancelTime = null,
            purchaseTime = paid,
            items = listOf(
                ReservationItemInfoDto(1, 2, firstReserved, firstReserved),
                ReservationItemInfoDto(2, 5, lastReserved, lastReserved)
            )
        )
        doReturn(reservationInfo).`when`(warehouseService).getReservationInfo(shoppingCart)

        mockMvc.perform(MockMvcRequestBuilders.get("/warehouse/reserve/$shoppingCart")
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("OK"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.shoppingCartId").value(shoppingCart))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.status").value("PAID"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.firstReserved").value(firstModified))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.lastReserved").value(lastModified))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.cancelTime").isEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.purchaseTime").value("2020-01-10T10:32:00"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.items[0].id").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.items[0].amount").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.items[0].firstModified").value(firstModified))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.items[0].lastModified").value(firstModified))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.items[1].id").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.items[1].amount").value(5))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.items[1].firstModified").value(lastModified))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.items[1].lastModified").value(lastModified))
    }

    @Test
    fun `Test cancel reservation for empty shopping cart ID`() {
        val badRequest = """{ "shoppingCartId": "  " }"""

        mockMvc.perform(MockMvcRequestBuilders.post("/warehouse/reserve/cancel")
            .content(badRequest)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("VALIDATION_FAILURE"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value(
                StringContains.containsString("Field 'shoppingCartId': Invalid shopping cart ID format")
            ))
    }

    @Test
    fun `Test cancel reservation for malformed shopping cart ID`() {
        val badRequest = """{ "shoppingCartId": "test-shopping-cart_1" }"""

        mockMvc.perform(MockMvcRequestBuilders.post("/warehouse/reserve/cancel")
            .content(badRequest)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("VALIDATION_FAILURE"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value(
                StringContains.containsString("Field 'shoppingCartId': Invalid shopping cart ID format")
            ))
    }

    @Test
    fun `Test cancel reservation`() {
        val request = """{ "shoppingCartId": "test-shopping-cart" }"""
        val item = ReservationItemDto(id = 3, amount = 5)
        val cancelTime = LocalDateTime.of(2020, 1, 1, 10, 30, 0)
        val cancelledOrder = CancelledReservation("test-shopping-cart", "Some message", cancelTime, listOf(item))

        doReturn(cancelledOrder).`when`(warehouseService).cancelReservation(any())
        mockMvc.perform(MockMvcRequestBuilders.post("/warehouse/reserve/cancel")
            .content(request)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("OK"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.shoppingCartId").value("test-shopping-cart"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.message").value("Some message"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.cancelTime").value("2020-01-01T10:30:00"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.items[0].id").value(item.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.items[0].amount").value(item.amount))
    }

    @Test
    fun `Test purchase reservation for empty shopping cart ID`() {
        val badRequest = """{ "shoppingCartId": "  " }"""

        mockMvc.perform(MockMvcRequestBuilders.post("/warehouse/reserve/purchase")
            .content(badRequest)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("VALIDATION_FAILURE"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value(
                StringContains.containsString("Invalid shopping cart ID format")
            ))
    }

    @Test
    fun `Test purchase reservation for malformed shopping cart ID`() {
        val badRequest = """{ "shoppingCartId": "test+shopping-cart-1" }"""

        mockMvc.perform(MockMvcRequestBuilders.post("/warehouse/reserve/purchase")
            .content(badRequest)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("VALIDATION_FAILURE"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value(
                StringContains.containsString("Field 'shoppingCartId': Invalid shopping cart ID format")
            ))
    }

    @Test
    fun `Test purchase reservation`() {
        val request = """{ "shoppingCartId": "test-shopping-cart" }"""
        val item = ReservationItemDto(id = 3, amount = 5)
        val purchaseTime = LocalDateTime.of(2020, 1, 1, 10, 30, 0)
        val purchasedOrder = PurchasedReservation("test-shopping-cart", "Some message", purchaseTime, listOf(item))

        doReturn(purchasedOrder).`when`(warehouseService).finishPurchase(any())
        mockMvc.perform(MockMvcRequestBuilders.post("/warehouse/reserve/purchase")
            .content(request)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("OK"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.shoppingCartId").value("test-shopping-cart"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.message").value("Some message"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.purchaseTime").value("2020-01-01T10:30:00"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.items[0].id").value(item.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.items[0].amount").value(item.amount))
    }
}
