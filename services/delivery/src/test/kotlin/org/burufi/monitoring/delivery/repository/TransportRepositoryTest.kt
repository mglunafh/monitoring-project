package org.burufi.monitoring.delivery.repository

import org.assertj.core.api.Assertions.assertThat
import org.burufi.monitoring.delivery.TEST_GAZELLE
import org.burufi.monitoring.delivery.TEST_MARK
import org.burufi.monitoring.delivery.TEST_TRUCK
import org.burufi.monitoring.delivery.model.Transport
import org.burufi.monitoring.delivery.model.TransportStatus.AVAILABLE
import org.burufi.monitoring.delivery.model.TransportStatus.DELIVERING
import org.burufi.monitoring.delivery.model.TransportStatus.RETURNING
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest
class TransportRepositoryTest {

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    @Autowired
    lateinit var repo: TransportRepository

    @Test
    fun `test first available, empty table`() {
        val result = repo.findFirstByTransportTypeMarkAndStatus(TEST_MARK)
        assertThat(result).isNull()
    }

    @Test
    fun `test first available`() {
        val truckType = TEST_TRUCK.copy()
        testEntityManager.persist(truckType)

        val truck = Transport(transportType = truckType)
        testEntityManager.persist(truck)

        val result = repo.findFirstByTransportTypeMarkAndStatus(TEST_MARK)
        requireNotNull(result)
        assertThat(result).isEqualTo(truck)
    }

    @Test
    fun `test first available, multiple transport units of the same type`() {
        val truckType = TEST_TRUCK.copy()
        testEntityManager.persist(truckType)
        val gazelleType = TEST_GAZELLE.copy()
        testEntityManager.persist(gazelleType)

        val busy = Transport(transportType = truckType, status = DELIVERING)
        val first = Transport(transportType = truckType, status = AVAILABLE)
        val second = Transport(transportType = truckType, status = AVAILABLE)
        val returning = Transport(transportType = truckType, status = RETURNING)
        val gazelle = Transport(transportType = gazelleType, status = AVAILABLE)

        listOf(busy, first, second, returning, gazelle).forEach { testEntityManager.persist(it) }

        val result = repo.findFirstByTransportTypeMarkAndStatus(TEST_MARK)
        requireNotNull(result)
        assertThat(result).isEqualTo(first)
    }
}
