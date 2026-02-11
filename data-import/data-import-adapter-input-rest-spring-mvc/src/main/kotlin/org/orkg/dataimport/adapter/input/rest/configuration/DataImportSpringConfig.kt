package org.orkg.dataimport.adapter.input.rest.configuration

import org.orkg.dataimport.adapter.input.rest.json.DataImportJacksonModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.JacksonModule

@Configuration
class DataImportSpringConfig {
    @Bean
    fun dataImportJacksonModule(): JacksonModule = DataImportJacksonModule()
}
