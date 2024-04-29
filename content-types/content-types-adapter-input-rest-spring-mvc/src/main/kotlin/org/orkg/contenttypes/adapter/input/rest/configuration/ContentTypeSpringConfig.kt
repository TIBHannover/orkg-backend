package org.orkg.contenttypes.adapter.input.rest.configuration

import com.fasterxml.jackson.databind.Module
import org.orkg.contenttypes.adapter.input.rest.json.ContentTypeJacksonModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ContentTypeSpringConfig {
    @Bean
    fun contentTypeJacksonModule(): Module = ContentTypeJacksonModule()
}
