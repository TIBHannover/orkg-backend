package org.orkg.common.configuration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.module.kotlin.KotlinFeature.NewStrictNullChecks
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.orkg.common.json.CommonJacksonModule
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

@Configuration
class CommonSpringConfig {
    @Bean
    fun commonJacksonModule(): Module = CommonJacksonModule()

    @Bean
    fun kotlinJacksonModule(): Module = KotlinModule.Builder()
        .enable(NewStrictNullChecks)
        .build()

    @Bean
    fun jacksonCustomizer(): Jackson2ObjectMapperBuilderCustomizer =
        Jackson2ObjectMapperBuilderCustomizer { builder: Jackson2ObjectMapperBuilder ->
            builder.failOnUnknownProperties(true)
            builder.featuresToDisable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        }
}
