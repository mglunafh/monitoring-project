package org.burufi.monitoring.delivery.model

import java.math.BigDecimal

/**
 * Represents a piece of transport and its technical characteristics.
 *
 * @property category Basic transport category.
 * @property mark Human-readable text definition of the transport e.g. "Volvo FH13 2012"
 * @property maxCargo Maximum amount of cargo to carry.
 * @property maxDistance Maximum distance this transport type unit can cover.
 * @property speed Average transport velocity.
 * @property pricePerDistance Price per unit of distance.
 *
 * @see TransportCategory
 */
data class TransportType(
    val id: Int,
    val category: TransportCategory,
    val mark: String,
    val maxCargo: Int,
    val maxDistance: Int,
    val speed: Int,
    val pricePerDistance: BigDecimal
)
