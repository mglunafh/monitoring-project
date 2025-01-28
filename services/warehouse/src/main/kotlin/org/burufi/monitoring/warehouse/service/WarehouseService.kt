package org.burufi.monitoring.warehouse.service

import org.burufi.monitoring.dto.warehouse.CancelledReservation
import org.burufi.monitoring.dto.warehouse.ContractInfo
import org.burufi.monitoring.dto.warehouse.GoodsItemDto
import org.burufi.monitoring.dto.warehouse.PurchasedReservation
import org.burufi.monitoring.dto.warehouse.RegisterContractRequest
import org.burufi.monitoring.dto.warehouse.RegisteredContract
import org.burufi.monitoring.dto.warehouse.ReservationInfo
import org.burufi.monitoring.dto.warehouse.ReservationItemDto
import org.burufi.monitoring.dto.warehouse.ReservationItemInfoDto
import org.burufi.monitoring.dto.warehouse.ReserveItemRequest
import org.burufi.monitoring.dto.warehouse.SupplierDto
import org.burufi.monitoring.warehouse.dao.WarehouseDao
import org.burufi.monitoring.warehouse.dao.record.ReserveStatus.CANCELLED
import org.burufi.monitoring.warehouse.dao.record.ReserveStatus.PAID
import org.burufi.monitoring.warehouse.dao.record.ReserveStatus.RESERVED
import org.burufi.monitoring.warehouse.exception.FailureType
import org.burufi.monitoring.warehouse.exception.WarehouseException
import org.burufi.monitoring.warehouse.mapper.WarehouseMapper
import org.postgresql.util.PSQLException
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.BatchUpdateException
import java.time.LocalDateTime

@Service
class WarehouseService(private val dao: WarehouseDao) {

    @Transactional
    fun getSuppliers(): List<SupplierDto> {
        val result = dao.getSuppliersList()
        return result.map { WarehouseMapper.map(it) }
    }

    @Transactional
    fun getGoods(): List<GoodsItemDto> {
        val result = dao.getGoodsList()
        return result.map { WarehouseMapper.map(it) }
    }

    @Transactional
    fun registerContract(contract: RegisterContractRequest): RegisteredContract {
        val supplierId = contract.supplierId
        val items = contract.items
        val contractCost = contract.totalCost

        val supplierExists = dao.supplierExists(supplierId)
        if (!supplierExists) throw WarehouseException(FailureType.SupplierIdNotFound)

        val signDate = LocalDateTime.now()
        val contractId = dao.createContract(supplierId, signDate, contractCost)
        try {
            dao.registerContractItems(contractId, items)
        } catch (ex: DataAccessException) {
            // the specific concern is an attempt to register a non-existent item ID inside a batch update.
            val buex = ex.cause as? BatchUpdateException
            val cause = buex?.cause as? PSQLException
            val errorContainer = cause?.serverErrorMessage

            throw when {
                errorContainer?.constraint == "goods_in_contract_item_id_fkey"
                    -> WarehouseException(FailureType.ProductIdNotFound(errorContainer.detail))
                else -> WarehouseException(FailureType.GenericDatabaseFailure(ex))
            }
        }

        return RegisteredContract(contractId, signDate, contractCost)
    }

    @Transactional
    fun getContractInfo(contractId: Int): ContractInfo? {
        return dao.getContractInfo(contractId)
    }

    @Transactional
    fun reserve(request: ReserveItemRequest): Boolean {
        val itemExists = dao.itemExists(request.itemId)
        if (!itemExists) return false

        val reserveTime = LocalDateTime.now()
        try {
            dao.reserveItem(request.shoppingCartId, request.itemId, request.amount, reserveTime)
            return true
        } catch (ex: Exception) {
            // the specific concern is to reserve too many items
            val psqlErrorContainer = (ex.cause as? PSQLException)?.serverErrorMessage
            throw when {
                psqlErrorContainer?.constraint == "goods_amount_check" -> WarehouseException(FailureType.ReserveTooManyItems)
                else -> WarehouseException(FailureType.GenericDatabaseFailure(ex))
            }
        }
    }

    @Transactional
    fun cancelReservation(shoppingCartId: String): CancelledReservation {
        val reservationProcessed = dao.isReservationProcessed(shoppingCartId)
        if (reservationProcessed) {
            return CancelledReservation(shoppingCartId, "Reservation does not exist or has already been processed")
        }

        val cancelTime = LocalDateTime.now()
        val cancelledItems = dao.cancelReservation(shoppingCartId, cancelTime)

        return CancelledReservation(
            shoppingCartId = shoppingCartId,
            message = "Reservation was successfully cancelled",
            cancelTime = cancelTime,
            items = cancelledItems.map { ReservationItemDto(it.itemId, it.amount.n) }
        )
    }

    @Transactional
    fun finishPurchase(shoppingCartId: String): PurchasedReservation {
        val reservationProcessed = dao.isReservationProcessed(shoppingCartId)
        if (reservationProcessed) {
            return PurchasedReservation(shoppingCartId, "Reservation does not exist or has already been processed")
        }
        val purchaseTime = LocalDateTime.now()
        val purchasedItems = dao.purchaseReservation(shoppingCartId, purchaseTime)

        return PurchasedReservation(
            shoppingCartId = shoppingCartId,
            message = "Reservation was successfully purchased",
            purchaseTime = purchaseTime,
            items = purchasedItems.map { ReservationItemDto(it.itemId, it.amount.n) }
        )
    }

    @Transactional
    fun getReservationInfo(shoppingCartId: String): ReservationInfo? {
        val items = dao.getReservationInfo(shoppingCartId)
        if (items.isEmpty()) return null

        val statuses = items.map { it.status }.distinct()
        val reservationStatus = when {
            PAID in statuses && CANCELLED in statuses ->
                throw IllegalArgumentException("Reservation '$shoppingCartId' is malformed, it has both final statuses.")
            PAID in statuses -> PAID
            CANCELLED in statuses -> CANCELLED
            else -> RESERVED
        }
        val firstReserved   = items.filter { it.status == RESERVED }.minOf { it.firstModified }
        val lastReserved    = items.filter { it.status == RESERVED }.maxOf { it.lastModified }
        val cancelTime      = items.filter { it.status == CANCELLED }.maxOfOrNull { it.lastModified }
        val purchaseTime    = items.filter { it.status == PAID }.maxOfOrNull { it.lastModified }

        return ReservationInfo(
            shoppingCartId = shoppingCartId,
            status = reservationStatus.name,
            firstReserved = firstReserved,
            lastReserved = lastReserved,
            cancelTime = cancelTime,
            purchaseTime = purchaseTime,
            items = items
                .filter { it.status == RESERVED }
                .map { ReservationItemInfoDto(it.itemId, it.amount.n, it.firstModified, it.lastModified) }
        )
    }
}
