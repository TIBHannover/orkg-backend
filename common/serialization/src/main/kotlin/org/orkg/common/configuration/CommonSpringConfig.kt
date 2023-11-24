package org.orkg.common.configuration

import com.fasterxml.jackson.databind.Module
import org.orkg.common.json.CommonJacksonModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CommonSpringConfig {
    @Bean
    fun commonJacksonModule(): Module = CommonJacksonModule()
}
