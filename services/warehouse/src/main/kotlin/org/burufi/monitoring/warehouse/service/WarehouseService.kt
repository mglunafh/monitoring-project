package org.burufi.monitoring.warehouse.service

import org.burufi.monitoring.dto.warehouse.CancelledReservation
import org.burufi.monitoring.dto.warehouse.CancelledReservationItemDto
import org.burufi.monitoring.dto.warehouse.ContractInfo
import org.burufi.monitoring.dto.warehouse.GoodsItemDto
import org.burufi.monitoring.dto.warehouse.RegisterContractRequest
import org.burufi.monitoring.dto.warehouse.RegisteredContract
import org.burufi.monitoring.dto.warehouse.ReserveItemRequest
import org.burufi.monitoring.dto.warehouse.SupplierDto
import org.burufi.monitoring.warehouse.dao.WarehouseDao
import org.burufi.monitoring.warehouse.exception.FailureType
import org.burufi.monitoring.warehouse.exception.WarehouseException
import org.burufi.monitoring.warehouse.mapper.WarehouseMapper
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
        if (!supplierExists) throw WarehouseException(FailureType.SUPPLIER_ID_NOT_FOUND)

        val signDate = LocalDateTime.now()
        val contractId = dao.createContract(supplierId, signDate, contractCost)
        try {
            dao.registerContractItems(contractId, items)
        } catch (ex: DataAccessException) {
            throw WarehouseException(FailureType.GENERIC_DATABASE_FAILURE, ex.message)
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
        dao.reserveItem(request.shoppingCartId, request.itemId, request.amount, reserveTime)

        return true
    }

    @Transactional
    fun cancelReservation(shoppingCartId: String): CancelledReservation {
        val reservationCancelled = dao.isReservationProcessed(shoppingCartId)
        val cancelTime = LocalDateTime.now()
        if (reservationCancelled) {
            return CancelledReservation(shoppingCartId, "Reservation does not exist or has already been processed", cancelTime)
        }

        val cancelledItems = dao.cancelReservation(shoppingCartId, cancelTime)

        return CancelledReservation(
            shoppingCartId = shoppingCartId,
            message = "Reservation was successfully cancelled",
            cancelTime = cancelTime,
            cancelledItems = cancelledItems.map { CancelledReservationItemDto(it.itemId, it.amount.n) }
        )
    }
}
