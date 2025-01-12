package org.burufi.monitoring.warehouse.controller

import jakarta.validation.Valid
import org.burufi.monitoring.dto.MyResponse
import org.burufi.monitoring.dto.MyResponse.Companion.toResponse
import org.burufi.monitoring.dto.ResponseCode
import org.burufi.monitoring.dto.warehouse.ListGoods
import org.burufi.monitoring.dto.warehouse.ListSuppliers
import org.burufi.monitoring.dto.warehouse.RegisterContractRequest
import org.burufi.monitoring.dto.warehouse.RegisteredContract
import org.burufi.monitoring.warehouse.service.WarehouseService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.Errors
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

    @PostMapping(
        "/contract",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun registerContract(
        @Valid @RequestBody contract: RegisterContractRequest,
        errors: Errors
    ): ResponseEntity<MyResponse<RegisteredContract>> {
        if (errors.hasGlobalErrors()) {
            val message = errors.globalErrors.joinToString(separator = ". ") { it.defaultMessage ?: "" }
            return ResponseEntity.badRequest().body(MyResponse.error(ResponseCode.VALIDATION_FAILURE, message))
        }
        if (errors.hasFieldErrors()) {
            val message = errors.fieldErrors.joinToString(separator = " ") {
                "Field '${it.field}': ${it.defaultMessage}, got '${it.rejectedValue}' instead."
            }
            return ResponseEntity.badRequest().body(MyResponse.error(ResponseCode.VALIDATION_FAILURE, message))
        }

        return ResponseEntity.ok(RegisteredContract(id = 138).toResponse())
    }
}
