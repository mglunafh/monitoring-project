package org.burufi.monitoring.warehouse.controller

import jakarta.validation.Valid
import org.burufi.monitoring.dto.MyResponse
import org.burufi.monitoring.dto.MyResponse.Companion.toResponse
import org.burufi.monitoring.dto.ResponseCode
import org.burufi.monitoring.dto.ResponseCode.VALIDATION_FAILURE
import org.burufi.monitoring.dto.warehouse.CancelledReservation
import org.burufi.monitoring.dto.warehouse.ProcessReserveRequest
import org.burufi.monitoring.dto.warehouse.ContractInfo
import org.burufi.monitoring.dto.warehouse.ContractItemOrderDto
import org.burufi.monitoring.dto.warehouse.ListGoods
import org.burufi.monitoring.dto.warehouse.ListSuppliers
import org.burufi.monitoring.dto.warehouse.PurchasedReservation
import org.burufi.monitoring.dto.warehouse.RegisterContractRequest
import org.burufi.monitoring.dto.warehouse.RegisteredContract
import org.burufi.monitoring.dto.warehouse.ReservationInfo
import org.burufi.monitoring.dto.warehouse.ReserveItemRequest
import org.burufi.monitoring.warehouse.service.WarehouseService
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.Errors
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
            return ResponseEntity.badRequest().body(MyResponse.error(VALIDATION_FAILURE, message))
        }
        if (errors.hasFieldErrors()) {
            val message = errors.fieldErrors.joinToString(separator = " ") {
                "Field '${it.field}': ${it.defaultMessage}, got '${it.rejectedValue}' instead."
            }
            return ResponseEntity.badRequest().body(MyResponse.error(VALIDATION_FAILURE, message))
        }
        val duplicateIds = duplicateIds(contract.items)
        if (duplicateIds.isNotEmpty()) {
            val message = "Items in the contract must be unique, there were duplicate IDs: $duplicateIds"
            return ResponseEntity.badRequest().body(MyResponse.error(VALIDATION_FAILURE, message))
        }

        val result = service.registerContract(contract)
        return ResponseEntity.ok(result.toResponse())
    }

    @GetMapping("/contract/{id}")
    fun getContractInfo(@PathVariable id: Int): ResponseEntity<MyResponse<ContractInfo>> {
        val result = service.getContractInfo(id)
        if (result == null) {
            val body = MyResponse.error<ContractInfo>(ResponseCode.NOT_FOUND, "Contract with id '$id' is not found")
            return ResponseEntity.status(HttpStatusCode.valueOf(404)).body(body)
        }
        return ResponseEntity.ok(result.toResponse())
    }

    @PostMapping("/reserve")
    fun reserveItem(
        @Valid @RequestBody reserveRequest: ReserveItemRequest,
        errors: Errors
    ): ResponseEntity<MyResponse<Nothing>> {
        if (errors.hasFieldErrors()) {
            val message = errors.fieldErrors.joinToString(separator = " ") {
                "Field '${it.field}': ${it.defaultMessage}, got '${it.rejectedValue}' instead."
            }
            return ResponseEntity.badRequest().body(MyResponse.error(VALIDATION_FAILURE, message))
        }
        val itemReserved = service.reserve(reserveRequest)
        if (!itemReserved) {
            val message = "Item with id '${reserveRequest.itemId}' does not exist."
            return ResponseEntity.badRequest().body(MyResponse.error(VALIDATION_FAILURE, message))
        }

        return ResponseEntity.ok(MyResponse(ResponseCode.OK, null, null))
    }

    @GetMapping("/reserve/{id}")
    fun reservationInfo(@PathVariable("id") shoppingCartId: String): ResponseEntity<MyResponse<ReservationInfo>> {
        val reservation = service.getReservationInfo(shoppingCartId)

        return if (reservation == null) {
            val body = MyResponse.error<ReservationInfo>(ResponseCode.NOT_FOUND, "Shopping cart with ID '$shoppingCartId' is not found")
            ResponseEntity.status(HttpStatusCode.valueOf(404)).body(body)
        } else {
            ResponseEntity.ok().body(reservation.toResponse())
        }
    }

    @PostMapping("/reserve/cancel")
    fun cancelReservation(
        @Valid @RequestBody cancelReserveRequest: ProcessReserveRequest,
        errors: Errors
    ) : ResponseEntity<MyResponse<CancelledReservation>> {
        if (errors.hasFieldErrors()) {
            val message = errors.fieldErrors.joinToString(separator = " ") {
                "Field '${it.field}': ${it.defaultMessage}, got '${it.rejectedValue}' instead."
            }
            return ResponseEntity.badRequest().body(MyResponse.error(VALIDATION_FAILURE, message))
        }

        val result = service.cancelReservation(cancelReserveRequest.shoppingCartId)
        return ResponseEntity.ok(result.toResponse())
    }

    @PostMapping("/reserve/purchase")
    fun purchaseReservation(
        @Valid @RequestBody purchaseRequest: ProcessReserveRequest,
        errors: Errors
    ) : ResponseEntity<MyResponse<PurchasedReservation>> {
        if (errors.hasFieldErrors()) {
            val message = errors.fieldErrors.joinToString(separator = " ") {
                "Field '${it.field}': ${it.defaultMessage}, got '${it.rejectedValue}' instead."
            }
            return ResponseEntity.badRequest().body(MyResponse.error(VALIDATION_FAILURE, message))
        }

        val result = service.finishPurchase(purchaseRequest.shoppingCartId)
        return ResponseEntity.ok(result.toResponse())
    }

    private fun duplicateIds(items: List<ContractItemOrderDto>): Set<Int> {
        val result = mutableSetOf<Int>()
        val temp = mutableSetOf<Int>()
        items.forEach { if (!temp.add(it.id)) result.add(it.id) }
        return result
    }
}
