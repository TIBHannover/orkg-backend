package org.orkg.community.adapter.input.rest.configuration

import org.orkg.community.adapter.input.rest.json.CommunityJacksonModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.JacksonModule

@Configuration
class CommunitySpringConfig {
    @Bean
    fun communityJacksonModule(): JacksonModule = CommunityJacksonModule()
}
