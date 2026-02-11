package org.orkg.contenttypes.adapter.input.rest.configuration

import org.orkg.contenttypes.adapter.input.rest.json.ContentTypeJacksonModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.JacksonModule

@Configuration
class ContentTypeSpringConfig {
    @Bean
    fun contentTypeJacksonModule(): JacksonModule = ContentTypeJacksonModule()
}
