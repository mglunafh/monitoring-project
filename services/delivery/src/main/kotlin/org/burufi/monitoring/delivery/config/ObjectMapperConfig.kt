package org.burufi.monitoring.delivery.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.time.format.DateTimeFormatter

@Configuration
class ObjectMapperConfig {

    companion object {
        const val DATE_FORMAT = "yyyy-MM-dd"
        const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"
    }

    @Bean
    fun jsonMapperCustomizer() = Jackson2ObjectMapperBuilderCustomizer {
        it.simpleDateFormat(DATE_TIME_FORMAT)
        it.serializers(LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
        it.serializers(LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)))
    }

    @Bean
    fun objectMapper(builder: Jackson2ObjectMapperBuilder): ObjectMapper {
        val result = builder.build<ObjectMapper>().apply {
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
        return result
    }
}