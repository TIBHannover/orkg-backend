package org.orkg.common.configuration

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.module.kotlin.KotlinFeature.StrictNullChecks
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.orkg.common.json.CommonJacksonModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CommonSpringConfig {
    @Bean
    fun commonJacksonModule(): Module = CommonJacksonModule()

    @Bean
    fun kotlinJacksonModule(): Module = KotlinModule.Builder()
        .enable(StrictNullChecks)
        .build()
}
