package org.burufi.monitoring.shop.service

import org.burufi.monitoring.shop.dao.CustomerRepository
import org.burufi.monitoring.shop.dto.CustomerDto

class CustomerService(private val userRepo: CustomerRepository) {

    suspend fun customers(): List<CustomerDto> {
        return userRepo.all().map { ShopMapper.convert(it) }
    }

    suspend fun customerByLogin(login: String): CustomerDto? {
        return userRepo.customerByName(login)?.let { ShopMapper.convert(it) }
    }
}
