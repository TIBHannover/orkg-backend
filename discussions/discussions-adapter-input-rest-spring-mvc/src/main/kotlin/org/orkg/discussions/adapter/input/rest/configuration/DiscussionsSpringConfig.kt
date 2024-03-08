package org.orkg.discussions.adapter.input.rest.configuration

import com.fasterxml.jackson.databind.Module
import org.orkg.discussions.adapter.input.rest.json.DiscussionsJacksonModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DiscussionsSpringConfig {
    @Bean
    fun discussionsJacksonModule(): Module = DiscussionsJacksonModule()
}
