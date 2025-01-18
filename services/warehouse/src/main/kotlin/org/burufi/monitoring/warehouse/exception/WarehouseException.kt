package org.burufi.monitoring.warehouse.exception

class WarehouseException(val failure: FailureType, val details: String?) : RuntimeException() {

    constructor(failure: FailureType) : this(failure, null)
}
