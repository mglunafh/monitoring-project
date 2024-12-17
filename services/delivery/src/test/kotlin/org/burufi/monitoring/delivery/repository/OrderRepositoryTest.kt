package org.burufi.monitoring.delivery.repository

import org.assertj.core.api.Assertions.assertThat
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

        val testOrder1 = TEST_ORDER.copy(shoppingCartId = "first-gazelle-order", transportType = gazelle)
        val testOrder2 = TEST_ORDER.copy(shoppingCartId = "second-gazelle-order", transportType = gazelle)
        val testOrder3 = TEST_ORDER.copy(shoppingCartId = "first-quick-order", transportType = smol)
        val finished = TEST_ORDER.copy(shoppingCartId = "finished-order", transportType = gazelle, status = DELIVERED)
        listOf(testOrder1, testOrder2, testOrder3, finished).forEach { testEntityManager.persist(it) }

        val resultRegistered = repo.findByStatus(REGISTERED)
        val resultSent = repo.findByStatus(SENT)
        val resultFinished = repo.findByStatus(DELIVERED)

        assertThat(resultRegistered).containsExactly(testOrder1, testOrder2, testOrder3)
        assertThat(resultSent).isEmpty()
        assertThat(resultFinished).containsExactly(finished)
    }
}
