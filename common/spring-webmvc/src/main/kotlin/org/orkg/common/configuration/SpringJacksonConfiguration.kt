package org.orkg.common.configuration

import com.fasterxml.jackson.databind.Module
import org.orkg.common.json.SpringJacksonModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SpringJacksonConfiguration {
    @Bean
    fun springJacksonModule(): Module = SpringJacksonModule()
}
