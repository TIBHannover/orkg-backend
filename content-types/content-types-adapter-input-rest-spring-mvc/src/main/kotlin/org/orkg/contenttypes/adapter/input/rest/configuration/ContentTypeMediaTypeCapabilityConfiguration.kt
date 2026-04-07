package org.orkg.contenttypes.adapter.input.rest.configuration

import org.orkg.common.MediaTypeCapabilityRegistry
import org.orkg.contenttypes.adapter.input.rest.TRANSPOSED_CAPABILITY
import org.springframework.context.annotation.Configuration

@Configuration
class ContentTypeMediaTypeCapabilityConfiguration(
    mediaTypeCapabilityRegistry: MediaTypeCapabilityRegistry,
) {
    init {
        mediaTypeCapabilityRegistry.register("text/csv", TRANSPOSED_CAPABILITY)
    }
}
