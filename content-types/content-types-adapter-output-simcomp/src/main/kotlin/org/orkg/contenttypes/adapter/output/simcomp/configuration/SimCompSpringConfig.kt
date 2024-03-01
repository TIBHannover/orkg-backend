package org.orkg.contenttypes.adapter.output.simcomp.configuration

import com.fasterxml.jackson.databind.Module
import org.orkg.contenttypes.adapter.output.simcomp.json.SimCompJacksonModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SimCompSpringConfig {
    @Bean
    fun simCompJacksonModule(): Module = SimCompJacksonModule()
}
