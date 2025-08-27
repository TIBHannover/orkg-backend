package org.orkg.dataimport.adapter.input.rest.configuration

import com.fasterxml.jackson.databind.Module
import org.orkg.dataimport.adapter.input.rest.json.DataImportJacksonModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataImportSpringConfig {
    @Bean
    fun dataImportJacksonModule(): Module = DataImportJacksonModule()
}
