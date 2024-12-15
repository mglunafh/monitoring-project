package org.burufi.monitoring.delivery.repository

import org.burufi.monitoring.delivery.model.TransportCategory
import org.burufi.monitoring.delivery.model.TransportType
import org.springframework.data.repository.CrudRepository

interface TransportTypeRepository : CrudRepository<TransportType, Int> {

    fun findByMark(mark: String): TransportType?

    fun findByCategory(category: TransportCategory): List<TransportType>

}
