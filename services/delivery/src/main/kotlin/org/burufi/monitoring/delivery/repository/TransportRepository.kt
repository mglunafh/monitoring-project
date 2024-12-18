package org.burufi.monitoring.delivery.repository

import org.burufi.monitoring.delivery.model.Transport
import org.burufi.monitoring.delivery.model.TransportStatus
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.CrudRepository

interface TransportRepository : CrudRepository<Transport, Int>, JpaSpecificationExecutor<Transport> {

    fun findFirstByTransportTypeMarkAndStatus(mark: String, status: TransportStatus = TransportStatus.AVAILABLE): Transport?

    fun findByStatusAndTransportTypeMark(status: TransportStatus, mark: String): List<Transport>

}
