package org.orkg.contenttypes.adapter.output.simcomp.configuration

import org.orkg.contenttypes.adapter.output.simcomp.json.SimCompJacksonModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.JacksonModule

@Configuration
class SimCompSpringConfig {
    @Bean
    fun simCompJacksonModule(): JacksonModule = SimCompJacksonModule()
}
