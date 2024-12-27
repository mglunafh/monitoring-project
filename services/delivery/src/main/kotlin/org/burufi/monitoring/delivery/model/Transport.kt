package org.burufi.monitoring.delivery.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table

/**
 * Vehicle instance owned by the delivery service.
 *
 * @property transportType Transport type.
 * @property status The current status of the transport unit.
 *
 * @see TransportType
 * @see TransportStatus
 */
@Entity
@Table(name = "park")
data class Transport(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "parkGenerator")
    @SequenceGenerator(name = "parkGenerator", sequenceName = "park_seq", allocationSize = 5)
    var id: Int? = null,

    @ManyToOne
    @JoinColumn
    var transportType: TransportType,

    var status: TransportStatus = TransportStatus.AVAILABLE
)
