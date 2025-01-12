package org.burufi.monitoring.warehouse.controller

import org.burufi.monitoring.dto.MyResponse
import org.burufi.monitoring.dto.MyResponse.Companion.toResponse
import org.burufi.monitoring.dto.warehouse.ListGoods
import org.burufi.monitoring.dto.warehouse.ListSuppliers
import org.burufi.monitoring.dto.warehouse.RegisterContractRequest
import org.burufi.monitoring.warehouse.service.WarehouseService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/warehouse")
class WarehouseController(private val service: WarehouseService) {

    @GetMapping("/suppliers", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getSuppliers(): MyResponse<ListSuppliers> {
        return ListSuppliers(service.getSuppliers()).toResponse()
    }

    @GetMapping("/goods", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getGoods(): MyResponse<ListGoods> {
        return ListGoods(service.getGoods()).toResponse()
    }

    @PostMapping("/contract",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun registerContract(@RequestBody contract: RegisterContractRequest) {
    }
}
