package org.burufi.monitoring.delivery.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(ObjectMapperConfig::class)
class MainConfig
