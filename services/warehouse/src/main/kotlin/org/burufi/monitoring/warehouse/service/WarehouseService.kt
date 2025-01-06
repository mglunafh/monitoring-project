package org.burufi.monitoring.warehouse.service

import org.burufi.monitoring.warehouse.dao.WarehouseDao
import org.burufi.monitoring.warehouse.dao.record.GoodsItem
import org.burufi.monitoring.warehouse.dao.record.Supplier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WarehouseService(private val dao: WarehouseDao) {

    @Transactional
    fun getSuppliers(): List<Supplier> {
        return dao.getSuppliersList()
    }

    @Transactional
    fun getGoods(): List<GoodsItem> {
        return dao.getGoodsList()
    }
}
