package org.burufi.monitoring.warehouse.integration

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration
import org.assertj.core.groups.Tuple
import org.burufi.monitoring.dto.MyResponse
import org.burufi.monitoring.dto.ResponseCode
import org.burufi.monitoring.dto.warehouse.CancelledReservation
import org.burufi.monitoring.dto.warehouse.ContractInfo
import org.burufi.monitoring.dto.warehouse.GoodsItemDto
import org.burufi.monitoring.dto.warehouse.ListGoods
import org.burufi.monitoring.dto.warehouse.ListSuppliers
import org.burufi.monitoring.dto.warehouse.ProcessReserveRequest
import org.burufi.monitoring.dto.warehouse.PurchasedReservation
import org.burufi.monitoring.dto.warehouse.RegisteredContract
import org.burufi.monitoring.dto.warehouse.ReservationInfo
import org.burufi.monitoring.dto.warehouse.ReservationItemDto
import org.burufi.monitoring.dto.warehouse.ReserveItemRequest
import org.burufi.monitoring.dto.warehouse.SupplierDto
import org.burufi.monitoring.warehouse.Utils.save
import org.burufi.monitoring.warehouse.dao.record.Amount
import org.burufi.monitoring.warehouse.dao.record.GoodsItem
import org.burufi.monitoring.warehouse.dao.record.ItemType
import org.burufi.monitoring.warehouse.dao.record.ReserveStatus
import org.burufi.monitoring.warehouse.dao.record.Supplier
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.Test

@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    properties = ["spring.datasource.hikari.schema=warehouse"]
)
class WarehouseIntegrationTest {

    companion object {
        val TEST_SUPPLIER = Supplier(-1, "Test Supplier", "Some test supplier")
        val TEST_ITEM = GoodsItem(-1, "Test Drink", ItemType.BEVERAGE, Amount(2), BigDecimal.valueOf(1000, 3))
        val MOCK_ITEM = GoodsItem(-1, "Mock Drug", ItemType.MEDICINE, Amount(10), BigDecimal.valueOf(50, 3))
        val HEADERS = HttpHeaders().apply {
            add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        }


        val GET_ENTITY = HttpEntity<Void>(null, null)

        inline fun <reified T: Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

        val typeRefSuppliers = typeRef<MyResponse<ListSuppliers>>()
        val typeRefGoods = typeRef<MyResponse<ListGoods>>()
        val typeRefNewContract = typeRef<MyResponse<RegisteredContract>>()
        val typeRefContractInfo = typeRef<MyResponse<ContractInfo>>()
        val typeRefReserveInfo = typeRef<MyResponse<ReservationInfo>>()
        val typeRefCancelReserve = typeRef<MyResponse<CancelledReservation>>()
        val typeRefPurchaseReserve = typeRef<MyResponse<PurchasedReservation>>()

        val postgresql: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:latest")
            .withInitScript("schema-pginit.sql")

        @JvmStatic
        @DynamicPropertySource
        fun configDatabase(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresql::getJdbcUrl)
            registry.add("spring.datasource.username", postgresql::getUsername)
            registry.add("spring.datasource.password", postgresql::getPassword)
        }

        @JvmStatic
        @BeforeAll
        fun startDatabase() {
            postgresql.start()
        }

        @JvmStatic
        @BeforeAll
        fun stopDatabase() {
            postgresql.stop()
        }
    }

    @Autowired
    lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @BeforeEach
    fun cleanUp() {
        jdbcTemplate.update("delete from goods_reserve", mapOf<String, Any>())
        jdbcTemplate.update("delete from goods_in_contract", mapOf<String, Any>())
        jdbcTemplate.update("delete from supply_contracts", mapOf<String, Any>())
        jdbcTemplate.update("delete from goods", mapOf<String, Any>())
        jdbcTemplate.update("delete from suppliers", mapOf<String, Any>())
    }

    @Test
    fun `Test list suppliers`() {
        val result = restTemplate.exchange("/warehouse/suppliers", HttpMethod.GET, GET_ENTITY, typeRefSuppliers)

        assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
        val emptySuppliers = requireNotNull(result.body)
        assertThat(emptySuppliers).extracting("responseCode", "errorMessage", "payload.suppliers")
            .isEqualTo(listOf(ResponseCode.OK, null, listOf<SupplierDto>()))

        val supplierId = jdbcTemplate.save(TEST_SUPPLIER)
        val result2 = restTemplate.exchange("/warehouse/suppliers", HttpMethod.GET, GET_ENTITY, typeRefSuppliers)

        assertThat(result2.statusCode).isEqualTo(HttpStatus.OK)
        val suppliers = requireNotNull(result2.body)

        assertThat(suppliers).extracting("responseCode", "errorMessage", "payload.suppliers")
            .isEqualTo(listOf(ResponseCode.OK, null, listOf(SupplierDto(supplierId, TEST_SUPPLIER.name, TEST_SUPPLIER.description))))
    }

    @Test
    fun `Test list goods`() {
        val emptyResult = restTemplate.exchange("/warehouse/goods", HttpMethod.GET, HttpEntity<Void>(null, null), typeRefGoods)

        assertThat(emptyResult.statusCode).isEqualTo(HttpStatus.OK)
        val emptyGoods = requireNotNull(emptyResult.body)
        assertThat(emptyGoods).extracting("responseCode", "errorMessage", "payload.goods")
            .isEqualTo(listOf(ResponseCode.OK, null, listOf<GoodsItemDto>()))

        val productId = jdbcTemplate.save(TEST_ITEM)
        val result = restTemplate.exchange("/warehouse/goods", HttpMethod.GET, GET_ENTITY, typeRefGoods)

        assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
        val goods = requireNotNull(result.body)

        assertThat(goods).extracting("responseCode", "errorMessage", "payload.goods")
            .isEqualTo(listOf(ResponseCode.OK, null, listOf(GoodsItemDto(productId, TEST_ITEM.name, TEST_ITEM.category.name, TEST_ITEM.amount.n, TEST_ITEM.weight))))
    }

    @Test
    fun `Test register supply contract and contract info`() {
        val supplierId = jdbcTemplate.save(TEST_SUPPLIER)
        val testId = jdbcTemplate.save(TEST_ITEM)
        val mockId = jdbcTemplate.save(MOCK_ITEM)

        val request = """
            {
                "supplierId": $supplierId,
                "items": [
                    { "id": $testId, "price": 15, "amount": 10 },
                    { "id": $mockId, "price": 35, "amount": 20 }
                ]
            }
        """.trimIndent()

        val headers = HttpHeaders().apply {
            add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        }
        val now = LocalDateTime.now()

        val responseNewContract = restTemplate.exchange("/warehouse/contract", HttpMethod.POST, HttpEntity(request, headers), typeRefNewContract)
        assertThat(responseNewContract.statusCode).isEqualTo(HttpStatus.OK)
        val contract = requireNotNull(responseNewContract.body)
        val payload = requireNotNull(contract.payload)

        val expectedCost = BigDecimal(15 * 10 + 35 * 20)
        assertThat(contract).extracting("responseCode", "errorMessage", "payload.totalCost")
            .isEqualTo(listOf(ResponseCode.OK, null, expectedCost))
        assertThat(payload.signDate).isAfterOrEqualTo(now)

        val responseGoods = restTemplate.exchange("/warehouse/goods", HttpMethod.GET, GET_ENTITY, typeRefGoods)
        assertThat(responseGoods.statusCode).isEqualTo(HttpStatus.OK)
        val goods = requireNotNull(responseGoods.body)

        val expectedItems = listOf(
            GoodsItemDto(testId, TEST_ITEM.name, TEST_ITEM.category.name, TEST_ITEM.amount.n + 10, TEST_ITEM.weight),
            GoodsItemDto(mockId, MOCK_ITEM.name, MOCK_ITEM.category.name, MOCK_ITEM.amount.n + 20, MOCK_ITEM.weight)
        )

        assertThat(goods).extracting("responseCode", "errorMessage", "payload.goods")
            .isEqualTo(listOf(ResponseCode.OK, null, expectedItems))

        val contractId = payload.id
        val signDate = payload.signDate
        val responseContractInfo = restTemplate.exchange("/warehouse/contract/$contractId", HttpMethod.GET, GET_ENTITY, typeRefContractInfo)
        assertThat(responseContractInfo.statusCode).isEqualTo(HttpStatus.OK)
        val contractInfo = requireNotNull(responseContractInfo.body)
        assertThat(contractInfo)
            .usingRecursiveComparison(
                RecursiveComparisonConfiguration.builder()
                    .withComparatorForType(bigDecimalComparator, BigDecimal::class.java)
                    .withComparatorForType(dateTimeComparator, LocalDateTime::class.java)
                    .build()
            )
            .isEqualTo(
            MyResponse(ResponseCode.OK, null, ContractInfo(contractId, TEST_SUPPLIER.name, signDate, expectedCost))
        )
    }

    @Test
    fun `Test reserve and cancel reservation`() {
        val testId = jdbcTemplate.save(TEST_ITEM)
        val mockId = jdbcTemplate.save(MOCK_ITEM)
        val shoppingCartId = "test-shopping-cart"

        val testReserve = ReserveItemRequest(shoppingCartId, testId, 1)
        val mockReserve1 = ReserveItemRequest(shoppingCartId, mockId, 2)
        val mockReserve2 = ReserveItemRequest(shoppingCartId, mockId, 3)

        // make a reservation
        val reserveTime = LocalDateTime.now()
        for (reserve in listOf(testReserve, mockReserve1, mockReserve2)) {
            val result = restTemplate.exchange("/warehouse/reserve", HttpMethod.POST, HttpEntity(reserve, HEADERS), String::class.java)
            assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
        }
        val itemsAggregate = listOf(tuple(testId, 1), tuple(mockId, 5))

        // check the available items
        checkInventory(
            GoodsItemDto(testId, TEST_ITEM.name, TEST_ITEM.category.name, TEST_ITEM.amount.n - 1, TEST_ITEM.weight),
            GoodsItemDto(mockId, MOCK_ITEM.name, MOCK_ITEM.category.name, MOCK_ITEM.amount.n - 5, MOCK_ITEM.weight)
        )

        // check the reservation itself
        checkReservation(shoppingCartId, reserveTime, ReserveStatus.RESERVED, itemsAggregate)

        // cancel
        // check cancellation info
        val cancelTime = LocalDateTime.now()
        val requestEntity = HttpEntity(ProcessReserveRequest(shoppingCartId), HEADERS)
        val cancellationResult =
            restTemplate.exchange("/warehouse/reserve/cancel", HttpMethod.POST, requestEntity, typeRefCancelReserve)
        assertThat(cancellationResult.statusCode).isEqualTo(HttpStatus.OK)
        val cancelResponse = requireNotNull(cancellationResult.body)
        assertThat(cancelResponse).extracting("responseCode", "errorMessage").isEqualTo(listOf(ResponseCode.OK, null))
        val cancelledReservation = requireNotNull(cancelResponse.payload)
        assertThat(cancelledReservation.cancelTime).isAfterOrEqualTo(cancelTime)
        val cancelledItemsList = listOf(ReservationItemDto(testId, 1), ReservationItemDto(mockId, 5))
        assertThat(cancelledReservation).extracting("shoppingCartId", "message", "items")
            .isEqualTo(listOf(shoppingCartId, "Reservation was successfully cancelled", cancelledItemsList))

        // check the reservation again
        checkReservation(shoppingCartId, reserveTime, ReserveStatus.CANCELLED, itemsAggregate, cancelTime = cancelTime)

        // check the available items again
        checkInventory(
            GoodsItemDto(testId, TEST_ITEM.name, TEST_ITEM.category.name, TEST_ITEM.amount.n, TEST_ITEM.weight),
            GoodsItemDto(mockId, MOCK_ITEM.name, MOCK_ITEM.category.name, MOCK_ITEM.amount.n, MOCK_ITEM.weight)
        )
    }

    @Test
    fun `Test reserve and purchase reservation`() {
        val testId = jdbcTemplate.save(TEST_ITEM)
        val mockId = jdbcTemplate.save(MOCK_ITEM)
        val shoppingCartId = "purchased-shopping-cart"

        val testReserve = ReserveItemRequest(shoppingCartId, testId, 2)
        val mockReserve1 = ReserveItemRequest(shoppingCartId, mockId, 3)
        val mockReserve2 = ReserveItemRequest(shoppingCartId, mockId, 1)
        val itemsAggregate = listOf(tuple(testId, 2), tuple(mockId, 4))

        // make a reservation
        val reserveTime = LocalDateTime.now()
        for (reserve in listOf(testReserve, mockReserve1, mockReserve2)) {
            val result = restTemplate.exchange("/warehouse/reserve", HttpMethod.POST, HttpEntity(reserve, HEADERS), String::class.java)
            assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
        }

        // check the available items
        checkInventory(
            GoodsItemDto(testId, TEST_ITEM.name, TEST_ITEM.category.name, TEST_ITEM.amount.n - 2, TEST_ITEM.weight),
            GoodsItemDto(mockId, MOCK_ITEM.name, MOCK_ITEM.category.name, MOCK_ITEM.amount.n - 4, MOCK_ITEM.weight)
        )

        // check the reservation itself
        checkReservation(shoppingCartId, reserveTime, ReserveStatus.RESERVED, itemsAggregate)

        // purchase
        // check the purchase details
        val purchaseTime = LocalDateTime.now()
        val requestEntity = HttpEntity(ProcessReserveRequest(shoppingCartId), HEADERS)
        val purchaseResult =
            restTemplate.exchange("/warehouse/reserve/purchase", HttpMethod.POST, requestEntity, typeRefPurchaseReserve)
        assertThat(purchaseResult.statusCode).isEqualTo(HttpStatus.OK)
        val response = requireNotNull(purchaseResult.body)
        assertThat(response).extracting("responseCode", "errorMessage").isEqualTo(listOf(ResponseCode.OK, null))
        val purchaseDetails = requireNotNull(response.payload)
        assertThat(purchaseDetails.purchaseTime).isAfterOrEqualTo(purchaseTime)
        val boughtItemsList = listOf(ReservationItemDto(testId, 2), ReservationItemDto(mockId, 4))
        assertThat(purchaseDetails).extracting("shoppingCartId", "message", "items")
            .isEqualTo(listOf(shoppingCartId, "Reservation was successfully purchased", boughtItemsList))

        // check the reservation again
        checkReservation(shoppingCartId, reserveTime, ReserveStatus.PAID, itemsAggregate, purchaseTime = purchaseTime)

        // check the available items again
        checkInventory(
            GoodsItemDto(testId, TEST_ITEM.name, TEST_ITEM.category.name, TEST_ITEM.amount.n - 2, TEST_ITEM.weight),
            GoodsItemDto(mockId, MOCK_ITEM.name, MOCK_ITEM.category.name, MOCK_ITEM.amount.n - 4, MOCK_ITEM.weight)
        )
    }

    private fun checkInventory(vararg expectedItems: GoodsItemDto) {
        val responseGoods = restTemplate.exchange("/warehouse/goods", HttpMethod.GET, GET_ENTITY, typeRefGoods)
        assertThat(responseGoods.statusCode).isEqualTo(HttpStatus.OK)
        val reservedGoodsAfter = requireNotNull(responseGoods.body)
        assertThat(reservedGoodsAfter).extracting("responseCode", "errorMessage", "payload.goods")
            .isEqualTo(listOf(ResponseCode.OK, null, expectedItems.toList()))
    }

    private fun checkReservation(
        shoppingCartId: String,
        reserveTime: LocalDateTime,
        status: ReserveStatus,
        items: List<Tuple>,
        cancelTime: LocalDateTime? = null,
        purchaseTime: LocalDateTime? = null
    ) {
        val reservationResult =
            restTemplate.exchange("/warehouse/reserve/$shoppingCartId", HttpMethod.GET, GET_ENTITY, typeRefReserveInfo)
        assertThat(reservationResult.statusCode).isEqualTo(HttpStatus.OK)
        val reservationResponse = requireNotNull(reservationResult.body)
        assertThat(reservationResponse).extracting("responseCode", "errorMessage").isEqualTo(listOf(ResponseCode.OK, null))

        val reservationInfo = requireNotNull(reservationResponse.payload)
        assertThat(reservationInfo).extracting("shoppingCartId", "status")
            .isEqualTo(listOf(shoppingCartId, status.name))
        assertThat(reservationInfo.firstReserved).isAfterOrEqualTo(reserveTime)
        assertThat(reservationInfo.lastReserved).isAfterOrEqualTo(reserveTime)
        assertDateIsNullOrAfter(reservationInfo.cancelTime, cancelTime)
        assertDateIsNullOrAfter(reservationInfo.purchaseTime, purchaseTime)
        assertThat(reservationInfo.items).extracting("id", "amount")
            .containsExactlyElementsOf(items)
    }

    private fun assertDateIsNullOrAfter(actual: LocalDateTime?, expected: LocalDateTime?) {
        if (expected != null) {
            assertThat(actual).isNotNull
            assertThat(actual).isAfterOrEqualTo(expected)
        } else {
            assertThat(actual).isNull()
        }
    }

    private val bigDecimalComparator : Comparator<BigDecimal> = Comparator { o1: BigDecimal?, o2: BigDecimal? ->
        when {
            o1 == null && o2 == null -> 0
            o1 == null -> -1
            o2 == null -> 1
            else -> o1.compareTo(o2)
        }
    }

    private val dateTimeComparator : Comparator<LocalDateTime> = Comparator { o1: LocalDateTime?, o2: LocalDateTime? ->
        when {
            o1 == null && o2 == null -> 0
            o1 == null -> -1
            o2 == null -> 1
            else -> o1.truncatedTo(ChronoUnit.MILLIS).compareTo(o2.truncatedTo(ChronoUnit.MILLIS))
        }
    }
}
