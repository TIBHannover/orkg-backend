package org.orkg.mediastorage.adapter.input.serialization.configuration

import org.orkg.mediastorage.adapter.input.serialization.json.MediaStorageJacksonModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.JacksonModule

@Configuration
class MediaStorageSpringConfig {
    @Bean
    fun mediaStorageJacksonModule(): JacksonModule = MediaStorageJacksonModule()
}
