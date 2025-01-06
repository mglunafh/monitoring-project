package org.burufi.monitoring.warehouse.controller

import org.burufi.monitoring.warehouse.dao.record.GoodsItem
import org.burufi.monitoring.warehouse.dao.record.Supplier
import org.burufi.monitoring.warehouse.service.WarehouseService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/warehouse")
class WarehouseController(private val service: WarehouseService) {

    @GetMapping("/suppliers", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getSuppliers(): List<Supplier> {
        return service.getSuppliers()
    }

    @GetMapping("/goods", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getGoods(): List<GoodsItem> {
        return service.getGoods()
    }
}
