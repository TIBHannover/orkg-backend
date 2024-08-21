package org.orkg.graph.adapter.input.rest.configuration

import jakarta.annotation.PostConstruct
import org.orkg.common.MediaTypeCapabilityRegistry
import org.orkg.graph.adapter.input.rest.FORMATTED_LABELS_CAPABILITY
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType

@Configuration
class GraphMediaTypeCapabilityConfiguration(
    private val mediaTypeCapabilityRegistry: MediaTypeCapabilityRegistry
) {
    @PostConstruct
    fun registerMediaTypeCapabilities() {
        mediaTypeCapabilityRegistry.register(MediaType.APPLICATION_JSON, FORMATTED_LABELS_CAPABILITY)
    }
}
