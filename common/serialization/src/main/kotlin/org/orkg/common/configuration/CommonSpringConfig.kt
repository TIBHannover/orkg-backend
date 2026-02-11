package org.orkg.common.configuration

import org.orkg.common.json.CommonJacksonModule
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinFeature
import tools.jackson.module.kotlin.KotlinModule

@Configuration
class CommonSpringConfig {
    @Bean
    fun commonJacksonModule(): JacksonModule = CommonJacksonModule()

    @Bean
    fun kotlinJacksonModule(): JacksonModule = KotlinModule.Builder()
        .enable(KotlinFeature.StrictNullChecks)
        .build()

    @Bean
    fun jacksonCustomizer(): JsonMapperBuilderCustomizer =
        JsonMapperBuilderCustomizer { builder: JsonMapper.Builder ->
            builder.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            builder.disable(DateTimeFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        }
}
