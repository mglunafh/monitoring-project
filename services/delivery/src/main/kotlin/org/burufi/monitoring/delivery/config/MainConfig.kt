package org.burufi.monitoring.delivery.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

@Configuration
class MainConfig {

    @Bean
    fun objectMapper(builder: Jackson2ObjectMapperBuilder): ObjectMapper {

        // TODO somehow dateformat here is not picked up, forced to @JsonFormat on response DTO field;
        //      also "yyyy-MM-dd HH:mm:ss.SSSZ" for time with zoneId
        val formatString = "yyyy-MM-dd HH:mm:ss.SSS"
        val dateFormat = SimpleDateFormat(formatString)
        val formatter = DateTimeFormatter.ofPattern(formatString)

        return builder.build<ObjectMapper>().apply {
            setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        }
    }
}
