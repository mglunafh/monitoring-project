package org.burufi.monitoring.delivery.repository

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.burufi.monitoring.delivery.GAZELLE_MARK
import org.burufi.monitoring.delivery.GEORADAR_MARK
import org.burufi.monitoring.delivery.TEST_GAZELLE
import org.burufi.monitoring.delivery.TEST_MARK
import org.burufi.monitoring.delivery.TEST_QUADCOPTER
import org.burufi.monitoring.delivery.TEST_TRUCK
import org.burufi.monitoring.delivery.model.TransportCategory.QUADCOPTER
import org.burufi.monitoring.delivery.model.TransportCategory.TRUCK
import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import kotlin.test.Test

@DataJpaTest
class TransportTypeRepositoryTest {

    @Autowired
    lateinit var repo: TransportTypeRepository

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    @Test
    fun `Test findByMark, no results`() {
        val findByMark = repo.findByMark("some")
        Assertions.assertNull(findByMark)
    }

    @Test
    fun `Test findByMark`() {
        val truck = TEST_TRUCK.copy()
        testEntityManager.persist(truck)

        val result = repo.findByMark(TEST_MARK)

        requireNotNull(result)
        assertThat(result).extracting("category", "mark", "maxCargo", "maxDistance", "speed")
            .isEqualTo(listOf(TRUCK, TEST_MARK, truck.maxCargo, truck.maxDistance, truck.speed))
    }

    @Test
    fun `Test findByCategory`() {
        val truck = TEST_TRUCK.copy()
        val gazelle = TEST_GAZELLE.copy()
        val smol = TEST_QUADCOPTER.copy()

        testEntityManager.persist(truck)
        testEntityManager.persist(gazelle)
        testEntityManager.persist(smol)

        val resultTrucks = repo.findByCategory(TRUCK)
        assertThat(resultTrucks).extracting("category", "mark", "maxCargo", "maxDistance")
            .containsExactly(
                tuple(TRUCK, TEST_MARK, 1111, 2222),
                tuple(TRUCK, GAZELLE_MARK, 500, 500)
            )

        val resultQuadcopters = repo.findByCategory(QUADCOPTER)
        assertThat(resultQuadcopters).extracting("category", "mark", "maxCargo", "maxDistance")
            .containsExactly(tuple(QUADCOPTER, GEORADAR_MARK, 5, 100))
    }
}
