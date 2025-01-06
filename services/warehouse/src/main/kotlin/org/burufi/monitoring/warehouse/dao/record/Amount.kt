package org.burufi.monitoring.warehouse.dao.record

@JvmInline
value class Amount(val n: Int) {
    init {
        require(n >= 0) { "Amount must be non-negative, got '$n' instead" }
    }
}
