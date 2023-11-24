package org.orkg.community.adapter.input.rest.configuration

import com.fasterxml.jackson.databind.Module
import org.orkg.community.adapter.input.rest.json.CommunityJacksonModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CommunitySpringConfig {
    @Bean
    fun communityJacksonModule(): Module = CommunityJacksonModule()
}
