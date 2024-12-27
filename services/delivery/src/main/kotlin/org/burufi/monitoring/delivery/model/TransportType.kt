package org.burufi.monitoring.delivery.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import java.math.BigDecimal

/**
 * Represents a piece of transport and its technical characteristics.
 *
 * @property category Basic transport category.
 * @property mark Human-readable text definition of the transport e.g. "Volvo FH13 2012". Must be unique.
 * @property maxCargo Maximum amount of cargo to carry.
 * @property maxDistance Maximum distance this transport type unit can cover.
 * @property speed Average transport velocity.
 * @property pricePerDistance Price per unit of distance.
 *
 * @see TransportCategory
 */
@Entity
data class TransportType(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transportTypeGenerator")
    @SequenceGenerator(name = "transportTypeGenerator", sequenceName = "transport_type_seq", allocationSize = 7)
    var id: Int? = null,

    var category: TransportCategory,

    @Column(unique = true)
    var mark: String,

    var maxCargo: Int,
    var maxDistance: Int,
    var speed: Int,
    var pricePerDistance: BigDecimal
)
