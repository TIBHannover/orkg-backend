package org.orkg.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.filter.CommonsRequestLoggingFilter

@Configuration
@Profile("development")
class RequestLoggingConfiguration {
    @Bean
    fun logFilter(): CommonsRequestLoggingFilter =
        CommonsRequestLoggingFilter().apply {
            setIncludeQueryString(true)
            setIncludePayload(true)
            setMaxPayloadLength(10_000)
            setIncludeHeaders(true)
            setAfterMessagePrefix("REQUEST DATA: ")
        }
}
