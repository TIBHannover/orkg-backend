package org.orkg.graph.adapter.input.rest.configuration

import org.orkg.graph.adapter.input.rest.json.GraphJacksonModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.JacksonModule

@Configuration
class GraphSpringConfig {
    @Bean
    fun graphJacksonModule(): JacksonModule = GraphJacksonModule()
}
