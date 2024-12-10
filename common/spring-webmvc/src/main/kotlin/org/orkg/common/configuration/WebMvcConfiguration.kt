package org.orkg.common.configuration

import org.orkg.common.MediaTypeCapabilitiesResolver
import org.orkg.common.MediaTypeCapabilityRegistry
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@Import(MediaTypeCapabilityRegistry::class)
class WebMvcConfiguration(
    private val mediaTypeCapabilityRegistry: MediaTypeCapabilityRegistry,
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(MediaTypeCapabilitiesResolver(mediaTypeCapabilityRegistry))
    }
}
