package org.burufi.monitoring.warehouse.service

import org.burufi.monitoring.dto.warehouse.GoodsItemDto
import org.burufi.monitoring.dto.warehouse.SupplierDto
import org.burufi.monitoring.warehouse.dao.WarehouseDao
import org.burufi.monitoring.warehouse.mapper.WarehouseMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
    fun registerContract() {
    }
}
