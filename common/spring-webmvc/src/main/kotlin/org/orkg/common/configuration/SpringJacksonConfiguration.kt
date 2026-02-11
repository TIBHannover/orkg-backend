package org.orkg.common.configuration

import org.orkg.common.json.SpringJacksonModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.JacksonModule

@Configuration
class SpringJacksonConfiguration {
    @Bean
    fun springJacksonModule(): JacksonModule = SpringJacksonModule()
}
