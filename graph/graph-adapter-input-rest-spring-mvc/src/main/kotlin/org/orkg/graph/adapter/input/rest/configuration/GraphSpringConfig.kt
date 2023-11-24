package org.orkg.graph.adapter.input.rest.configuration

import com.fasterxml.jackson.databind.Module
import org.orkg.graph.adapter.input.rest.json.GraphJacksonModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GraphSpringConfig {
    @Bean
    fun graphJacksonModule(): Module = GraphJacksonModule()
}
