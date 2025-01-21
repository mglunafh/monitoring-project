package org.burufi.monitoring.warehouse.dao

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.burufi.monitoring.dto.warehouse.ContractItemOrderDto
import org.burufi.monitoring.warehouse.dao.record.Amount
import org.burufi.monitoring.warehouse.dao.record.GoodsItem
import org.burufi.monitoring.warehouse.dao.record.ItemType
import org.burufi.monitoring.warehouse.dao.record.Supplier
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.jdbc.Sql
import org.testcontainers.containers.PostgreSQLContainer
import java.math.BigDecimal
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

        assertThat(dao.getSuppliersList()).extracting("name", "description")
            .contains(tuple(TEST_SUPPLIER.name, TEST_SUPPLIER.description))
    }

    @Test
    fun `Test goods`() {
        assertThat(dao.getGoodsList()).isEmpty()
        save(TEST_ITEM)

        val goodsList = dao.getGoodsList()
        assertThat(goodsList).extracting("name", "category", "amount", "weight")
            .contains(tuple(TEST_ITEM.name, TEST_ITEM.category, TEST_ITEM.amount.n, TEST_ITEM.weight))
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
        assertThat(goods).extracting("id", "amount")
            .contains(
                tuple(itemId1, TEST_ITEM.amount.n + 2),
                tuple(itemId2, MOCK_ITEM.amount.n + 3)
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
}
