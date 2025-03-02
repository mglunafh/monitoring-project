package org.burufi.monitoring.shop.service

import org.burufi.monitoring.shop.dto.CustomerDto
import org.burufi.monitoring.shop.model.Customer
import java.time.LocalDateTime

object ShopMapper {

    fun convert(customer: Customer) = CustomerDto(customer.id, customer.login, customer.registeredAt.convert())

    private fun LocalDateTime.convert() =
        kotlinx.datetime.LocalDateTime(year, monthValue, dayOfMonth, hour, minute, second, nano)
}