package org.burufi.monitoring.warehouse.dao

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.burufi.monitoring.dto.warehouse.ContractItemOrderDto
import org.burufi.monitoring.warehouse.dao.record.Amount
import org.burufi.monitoring.warehouse.dao.record.GoodsItem
import org.burufi.monitoring.warehouse.dao.record.ItemType
import org.burufi.monitoring.warehouse.dao.record.ReserveStatus
import org.burufi.monitoring.warehouse.dao.record.Supplier
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.postgresql.util.PSQLException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.jdbc.Sql
import org.testcontainers.containers.PostgreSQLContainer
import java.math.BigDecimal
import java.sql.BatchUpdateException
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.Test

@JdbcTest(
    properties = ["spring.datasource.hikari.schema=warehouse"]
)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Sql("classpath:schema-pginit.sql")
class WarehouseDaoTest {

    companion object {
        val TEST_SUPPLIER = Supplier(-1, "Test Supplier", "Some test supplier")
        val TEST_ITEM = GoodsItem(-1, "Test Drink", ItemType.BEVERAGE, Amount(2), BigDecimal.valueOf(1000, 3))
        val MOCK_ITEM = GoodsItem(-1, "Mock Drug", ItemType.MEDICINE, Amount(10), BigDecimal.valueOf(50, 3))

        val postgresql = PostgreSQLContainer("postgres:latest")

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

    lateinit var dao: WarehouseDao

    @BeforeEach
    fun init() {
        dao = WarehouseDao(jdbcTemplate)
    }

    @Test
    fun `Test suppliers`() {
        assertThat(dao.getSuppliersList()).isEmpty()
        save(TEST_SUPPLIER)

        assertThat(dao.getSuppliersList()).usingRecursiveComparison().ignoringFields("id")
            .isEqualTo(listOf(TEST_SUPPLIER))
    }

    @Test
    fun `Test goods`() {
        assertThat(dao.getGoodsList()).isEmpty()
        save(TEST_ITEM)

        val goodsList = dao.getGoodsList()
        assertThat(goodsList).usingRecursiveComparison().ignoringFields("id")
            .isEqualTo(listOf(TEST_ITEM))
    }

    @Test
    fun `Test supplier existence`() {
        val id = save(TEST_SUPPLIER)

        assertThat(dao.supplierExists(id)).isTrue
        assertThat(dao.supplierExists(id + 1)).isFalse
    }

    @Test
    fun `Test create contract`() {
        val supplierId = save(TEST_SUPPLIER)
        val signDate = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)

        val contractId = dao.createContract(supplierId, signDate, BigDecimal(50))

        val info = requireNotNull(dao.getContractInfo(contractId))
        assertThat(info).extracting("id", "supplier", "signDate", "totalCost")
            .contains(contractId, TEST_SUPPLIER.name, signDate, BigDecimal.valueOf(50).setScale(2))
    }

    @Test
    fun `Test register contract items`() {
        val supplierId = save(TEST_SUPPLIER)
        val itemId1 = save(TEST_ITEM)
        val itemId2 = save(MOCK_ITEM)
        val signDate = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        val contractId = dao.createContract(supplierId, signDate, BigDecimal(55))

        val contractItems = listOf(
            ContractItemOrderDto(itemId1, BigDecimal.valueOf(20), 2),
            ContractItemOrderDto(itemId2, BigDecimal.valueOf(5), 3)
        )

        dao.registerContractItems(contractId, contractItems)

        val goods = dao.getGoodsList()
        assertThat(goods).usingRecursiveComparison().ignoringFields("id")
            .isEqualTo(listOf(TEST_ITEM.plusItems(2), MOCK_ITEM.plusItems(3)))
    }

    @Test
    fun `Test register non-existent contract items`() {
        val supplierId = save(TEST_SUPPLIER)
        val itemId1 = save(TEST_ITEM)
        val signDate = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        val contractId = dao.createContract(supplierId, signDate, BigDecimal(55))
        val contractItems = listOf(
            ContractItemOrderDto(itemId1, BigDecimal.valueOf(20), 2),
            ContractItemOrderDto(itemId1 + 1, BigDecimal.valueOf(5), 3)
        )

        val errorResult = runCatching { dao.registerContractItems(contractId, contractItems) }

        assertThat(errorResult.exceptionOrNull())
            .isInstanceOf(DataIntegrityViolationException::class.java)
            .hasCauseInstanceOf(BatchUpdateException::class.java)
            .hasRootCauseInstanceOf(PSQLException::class.java)
            .extracting("cause.cause.serverErrorMessage")
            .extracting("table", "constraint")
            .isEqualTo(listOf("goods_in_contract", "goods_in_contract_item_id_fkey"))
    }

    @Test
    fun `Test item existence`() {
        val id = save(TEST_ITEM)

        assertThat(dao.itemExists(id)).isTrue
        assertThat(dao.itemExists(id + 1)).isFalse
    }

    @Test
    fun `Test reserve item`() {
        val id = save(TEST_ITEM)
        val shoppingCart = "test-shopping-cart-id"
        val reserveTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        dao.reserveItem(shoppingCart, id, 1, reserveTime)

        val itemInDB = persistedItem(id)
        assertThat(itemInDB).usingRecursiveComparison().ignoringFields("id")
            .isEqualTo(TEST_ITEM.minusItems(1))
    }

    @Test
    fun `Test reserve too much`() {
        val id = save(TEST_ITEM)
        val shoppingCart = "test-shopping-cart-id"
        val reserveTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)

        val errorResult = runCatching { dao.reserveItem(shoppingCart, id, TEST_ITEM.amount.n + 1, reserveTime) }

        assertThat(errorResult.exceptionOrNull())
            .isInstanceOf(DataIntegrityViolationException::class.java)
            .hasCauseInstanceOf(PSQLException::class.java)
            .extracting("cause.serverErrorMessage")
            .extracting("table", "constraint")
            .isEqualTo(listOf("goods", "goods_amount_check"))
    }

    @Test
    fun `Test status of ongoing reservation`() {
        val id = save(TEST_ITEM)
        val shoppingCart = "test-shopping-cart-id"
        val reserveTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        dao.reserveItem(shoppingCart, id, 1, reserveTime)

        val result = dao.isReservationProcessed(shoppingCart)

        assertThat(result).isFalse
    }

    @Test
    fun `Test non-existent reservation info`() {
        val result = dao.getReservationInfo("test-shopping-cart")
        assertThat(result).isEmpty()
    }

    @Test
    fun `Test reservation info`() {
        val testId = save(TEST_ITEM)
        val mockId = save(MOCK_ITEM)
        val shoppingCart = "test-shopping-cart-id"
        val reserveTime = LocalDateTime.now().minusMinutes(10).truncatedTo(ChronoUnit.SECONDS)
        dao.reserveItem(shoppingCart, testId, 1, reserveTime)
        dao.reserveItem(shoppingCart, mockId, 2, reserveTime.plusMinutes(1))
        dao.reserveItem(shoppingCart, mockId, 3, reserveTime.plusMinutes(2))
        dao.reserveItem(shoppingCart, testId, 1, reserveTime.plusMinutes(3))

        val result = dao.getReservationInfo(shoppingCart)

        assertThat(result).extracting("itemId", "amount", "firstModified", "lastModified", "status")
            .containsExactlyInAnyOrder(
                tuple(testId, 2, reserveTime, reserveTime.plusMinutes(3), ReserveStatus.RESERVED),
                tuple(mockId, 5, reserveTime.plusMinutes(1), reserveTime.plusMinutes(2), ReserveStatus.RESERVED)
            )

        val cancelTime = reserveTime.plusMinutes(5)
        dao.cancelReservation(shoppingCart, cancelTime)
    }

    @Test
    fun `Test cancel reservation`() {
        val testId = save(TEST_ITEM)
        val mockId = save(MOCK_ITEM)
        val shoppingCart = "test-shopping-cart-id"
        val reserveTime = LocalDateTime.now().minusMinutes(10).truncatedTo(ChronoUnit.SECONDS)
        dao.reserveItem(shoppingCart, testId, 1, reserveTime)
        dao.reserveItem(shoppingCart, mockId, 2, reserveTime.plusMinutes(1))
        dao.reserveItem(shoppingCart, mockId, 3, reserveTime.plusMinutes(2))
        dao.reserveItem(shoppingCart, testId, 1, reserveTime.plusMinutes(3))

        val itemsAfterReservation = listOf(testId, mockId).map { persistedItem(it) }
        assertThat(itemsAfterReservation).usingRecursiveComparison().ignoringFields("id")
            .isEqualTo(listOf(TEST_ITEM.minusItems(2), MOCK_ITEM.minusItems(5)))

        val cancelTime = reserveTime.plusMinutes(5)
        val cancelledItems = dao.cancelReservation(shoppingCart, cancelTime)

        assertThat(cancelledItems).extracting("shoppingCartId", "itemId", "amount")
            .containsExactly(tuple(shoppingCart, testId, 2), tuple(shoppingCart, mockId, 5))
        assertThat(dao.isReservationProcessed(shoppingCart)).isTrue

        val itemsAfterCancellation = listOf(testId, mockId).map { persistedItem(it) }
        assertThat(itemsAfterCancellation).usingRecursiveComparison().ignoringFields("id")
            .isEqualTo(listOf(TEST_ITEM, MOCK_ITEM))

        val reservationInfo = dao.getReservationInfo(shoppingCart)
        assertThat(reservationInfo).extracting("itemId", "amount", "firstModified", "lastModified", "status")
            .containsExactlyInAnyOrder(
                tuple(testId, 2, reserveTime, reserveTime.plusMinutes(3), ReserveStatus.RESERVED),
                tuple(testId, 2, cancelTime, cancelTime, ReserveStatus.CANCELLED),
                tuple(mockId, 5, reserveTime.plusMinutes(1), reserveTime.plusMinutes(2), ReserveStatus.RESERVED),
                tuple(mockId, 5, cancelTime, cancelTime, ReserveStatus.CANCELLED)
            )
    }

    @Test
    fun `Test purchase reservation`() {
        val testId = save(TEST_ITEM)
        val mockId = save(MOCK_ITEM)
        val shoppingCart = "test-shopping-cart-id"
        val reserveTime = LocalDateTime.now().minusMinutes(10).truncatedTo(ChronoUnit.SECONDS)
        dao.reserveItem(shoppingCart, testId, 1, reserveTime)
        dao.reserveItem(shoppingCart, mockId, 2, reserveTime.plusMinutes(1))
        dao.reserveItem(shoppingCart, mockId, 3, reserveTime.plusMinutes(2))
        dao.reserveItem(shoppingCart, testId, 1, reserveTime.plusMinutes(3))

        val itemsAfterReservation = listOf(testId, mockId).map { persistedItem(it) }
        assertThat(itemsAfterReservation).usingRecursiveComparison().ignoringFields("id")
            .isEqualTo(listOf(TEST_ITEM.minusItems(2), MOCK_ITEM.minusItems(5)))

        val purchaseTime = reserveTime.plusMinutes(5)
        val purchasedItems = dao.purchaseReservation(shoppingCart, purchaseTime)

        assertThat(purchasedItems).extracting("shoppingCartId", "itemId", "amount")
            .containsExactly(tuple(shoppingCart, testId, 2), tuple(shoppingCart, mockId, 5))
        assertThat(dao.isReservationProcessed(shoppingCart)).isTrue

        val itemsAfterPurchase = listOf(testId, mockId).map { persistedItem(it) }
        assertThat(itemsAfterPurchase).usingRecursiveComparison().ignoringFields("id")
            .isEqualTo(itemsAfterReservation)

        val reservationInfo = dao.getReservationInfo(shoppingCart)
        assertThat(reservationInfo).extracting("itemId", "amount", "firstModified", "lastModified", "status")
            .containsExactlyInAnyOrder(
                tuple(testId, 2, reserveTime, reserveTime.plusMinutes(3), ReserveStatus.RESERVED),
                tuple(testId, 2, purchaseTime, purchaseTime, ReserveStatus.PAID),
                tuple(mockId, 5, reserveTime.plusMinutes(1), reserveTime.plusMinutes(2), ReserveStatus.RESERVED),
                tuple(mockId, 5, purchaseTime, purchaseTime, ReserveStatus.PAID)
            )
    }

    private fun save(supplier: Supplier): Int {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update(
            "insert into suppliers(name, description) values (:name, :description)",
            MapSqlParameterSource(mapOf("name" to supplier.name, "description" to supplier.description)),
            keyHolder,
            arrayOf("id")
        )
        return keyHolder.key as Int
    }

    private fun save(item: GoodsItem): Int {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update("insert into goods(name, category, amount, weight) values (:n, :c, :a, :w)",
            MapSqlParameterSource(mapOf("n" to item.name, "c" to item.category.name, "a" to item.amount.n, "w" to item.weight)),
            keyHolder,
            arrayOf("id")
        )
        return keyHolder.key as Int
    }

    private fun persistedItem(id: Int): GoodsItem {
        return jdbcTemplate.queryForObject("select * from goods where id = :id", mapOf("id" to id), RowMappers.GoodsItemRowMapper)!!
    }

    private fun GoodsItem.plusItems(n: Int) = this.copy(amount = Amount(amount.n + n))

    private fun GoodsItem.minusItems(n: Int) = this.copy(amount = Amount(amount.n - n))

}
