package org.burufi.monitoring.delivery.repository

import org.burufi.monitoring.delivery.model.Transport
import org.springframework.data.repository.CrudRepository

interface TransportRepository : CrudRepository<Transport, Int> {

    // TODO: find available of given mark
    // TODO: find available of given category

}