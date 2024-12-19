package org.burufi.monitoring.delivery.model

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

object EnumConverters {

    @Converter(autoApply = true)
    object OrderStatusConverter : AttributeConverter<OrderStatus, String> {
        override fun convertToDatabaseColumn(attribute: OrderStatus?): String? {
            return attribute?.code
        }

        override fun convertToEntityAttribute(code: String?): OrderStatus? {
            return code?.let { OrderStatus.entries.first { it.code == code } }
        }
    }

    @Converter(autoApply = true)
    object TransportStatusConverter : AttributeConverter<TransportStatus, String> {
        override fun convertToDatabaseColumn(attribute: TransportStatus?): String? {
            return attribute?.code
        }

        override fun convertToEntityAttribute(code: String?): TransportStatus? {
            return code?.let { TransportStatus.entries.first { it.code == code } }
        }
    }

    @Converter(autoApply = true)
    object TransportCategoryConverter : AttributeConverter<TransportCategory, String> {
        override fun convertToDatabaseColumn(attribute: TransportCategory?): String? {
            return attribute?.name
        }

        override fun convertToEntityAttribute(code: String?): TransportCategory? {
            return code?.let { TransportCategory.entries.first { it.name == code } }
        }
    }
}
