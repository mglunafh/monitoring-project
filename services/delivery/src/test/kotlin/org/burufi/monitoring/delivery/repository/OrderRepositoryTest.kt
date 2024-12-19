package org.burufi.monitoring.delivery.repository

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration
import org.burufi.monitoring.delivery.GAZELLE_MARK
import org.burufi.monitoring.delivery.GEORADAR_MARK
import org.burufi.monitoring.delivery.TEST_GAZELLE
import org.burufi.monitoring.delivery.TEST_ORDER
import org.burufi.monitoring.delivery.TEST_QUADCOPTER
import org.burufi.monitoring.delivery.TEST_SHOPPING_CART
import org.burufi.monitoring.delivery.model.OrderStatus.DELIVERED
import org.burufi.monitoring.delivery.model.OrderStatus.REGISTERED
import org.burufi.monitoring.delivery.model.OrderStatus.SENT
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import java.math.BigDecimal
import kotlin.test.Test

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    lateinit var repo: OrderRepository

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    @Test
    fun `test findByShoppingCartId empty`() {
        val absentOrder = repo.findByShoppingCartId("some-cart-id")
        assertThat(absentOrder).isNull()
    }

    @Test
    fun `test findByShoppingCartId`() {
        val gazelle = TEST_GAZELLE.copy()
        testEntityManager.persist(gazelle)
        val testOrder = TEST_ORDER.copy(transportType = gazelle)
        testEntityManager.persist(testOrder)

        val result = repo.findByShoppingCartId(TEST_SHOPPING_CART)
        requireNotNull(result)
        assertThat(result).extracting("shoppingCartId", "status", "distance", "orderTime")
            .contains(TEST_SHOPPING_CART, REGISTERED, 100, TEST_ORDER.orderTime)
        assertThat(result.transportType).isEqualTo(gazelle)
    }

    @Test
    fun `test findByStatus`() {
        val gazelle = TEST_GAZELLE.copy()
        val smol = TEST_QUADCOPTER.copy()
        testEntityManager.persist(gazelle)
        testEntityManager.persist(smol)

        val order1 = TEST_ORDER.copy(shoppingCartId = "first-gazelle-order", transportType = gazelle)
        val order2 = TEST_ORDER.copy(shoppingCartId = "second-gazelle-order", transportType = gazelle)
        val sentOrder = TEST_ORDER.copy(shoppingCartId = "first-sent-order", transportType = smol, status = SENT)
        val order4 = TEST_ORDER.copy(shoppingCartId = "first-quick-order", transportType = smol)
        val finished = TEST_ORDER.copy(shoppingCartId = "finished-order", transportType = gazelle, status = DELIVERED)
        listOf(order1, order2, sentOrder, order4, finished).forEach { testEntityManager.persist(it) }

        val resultRegistered = repo.findByStatusIn(REGISTERED)
        val resultSent = repo.findByStatusIn(SENT)
        val resultFinished = repo.findByStatusIn(DELIVERED)
        val resultOngoing = repo.findByStatusIn(REGISTERED, SENT)

        assertThat(resultRegistered).containsExactly(order1, order2, order4)
        assertThat(resultSent).containsExactly(sentOrder)
        assertThat(resultFinished).containsExactly(finished)
        assertThat(resultOngoing).containsExactly(order1, order2, sentOrder, order4)
    }

    @Test
    fun `test findAwaitingOrder, queue is empty`() {
        val result = repo.findAwaitingOrder(GAZELLE_MARK)
        assertThat(result).isNull()
    }

    @Test
    fun `test findAwaitingOrder`() {
        val gazelle = TEST_GAZELLE.copy()
        val georadar = TEST_QUADCOPTER.copy()
        testEntityManager.persist(gazelle)
        testEntityManager.persist(georadar)

        val georadarOrder = TEST_ORDER.copy(shoppingCartId = "first-georadar-order", transportType = georadar)
        val order1 = TEST_ORDER.copy(shoppingCartId = "first-sent-order", transportType = gazelle, status = SENT)
        val order2 = TEST_ORDER.copy(shoppingCartId = "second-gazelle-order", transportType = gazelle)
        val order3 = TEST_ORDER.copy(shoppingCartId = "third-gazelle-order", transportType = gazelle)
        listOf(georadarOrder, order1, order2, order3).forEach { testEntityManager.persist(it) }

        val gazResult = repo.findAwaitingOrder(GAZELLE_MARK)
        assertThat(gazResult).isEqualTo(order2)
        val geoResult = repo.findAwaitingOrder(GEORADAR_MARK)
        assertThat(geoResult).isEqualTo(georadarOrder)
    }

    @Test
    fun `Test order statistics`() {
        val gazelle = TEST_GAZELLE.copy()
        val georadar = TEST_QUADCOPTER.copy()
        testEntityManager.persist(gazelle)
        testEntityManager.persist(georadar)

        val completed1 = TEST_ORDER.copy(shoppingCartId = "first-finished", transportType = gazelle, status = DELIVERED, cost = BigDecimal(120))
        val completed2 = TEST_ORDER.copy(shoppingCartId = "second-finished", transportType = georadar, status = DELIVERED, cost = BigDecimal(330))

        val sent1 = TEST_ORDER.copy(shoppingCartId = "first-sent", transportType = gazelle, status = SENT)

        val order1 = TEST_ORDER.copy(shoppingCartId = "first-registered-order", transportType = georadar)
        val order2 = TEST_ORDER.copy(shoppingCartId = "second-registered-order", transportType = gazelle)
        val order3 = TEST_ORDER.copy(shoppingCartId = "third-registered-order", transportType = georadar)

        listOf(completed1, completed2, sent1, order1, order2, order3).forEach { testEntityManager.persist(it) }

        val expenses = repo.findOrderStatistics()

        assertThat(expenses).extracting("status", "orderCount", "totalCost")
            .usingRecursiveFieldByFieldElementComparator(
                RecursiveComparisonConfiguration.builder()
                    .withComparatorForType(bigDecimalComparator, BigDecimal::class.java)
                    .build())
            .contains(
                tuple(REGISTERED, 3, null),
                tuple(SENT, 1, null),
                tuple(DELIVERED, 2, BigDecimal(450)),
        )
    }

    private val bigDecimalComparator : Comparator<BigDecimal> = Comparator { o1: BigDecimal?, o2: BigDecimal? ->
        when {
            o1 == null && o2 == null -> 0
            o1 == null -> -1
            o2 == null -> 1
            else -> o1.compareTo(o2)
        }
    }
}
