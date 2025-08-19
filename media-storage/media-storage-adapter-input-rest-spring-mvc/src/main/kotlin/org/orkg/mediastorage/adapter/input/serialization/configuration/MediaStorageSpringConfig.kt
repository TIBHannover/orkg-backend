package org.orkg.mediastorage.adapter.input.serialization.configuration

import com.fasterxml.jackson.databind.Module
import org.orkg.mediastorage.adapter.input.serialization.json.MediaStorageJacksonModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MediaStorageSpringConfig {
    @Bean
    fun mediaStorageJacksonModule(): Module = MediaStorageJacksonModule()
}
