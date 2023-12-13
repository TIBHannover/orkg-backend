package org.orkg.configuration

import java.time.Clock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DateAndTimeConfig {
    @Bean
    fun clock(): Clock = Clock.systemDefaultZone()
}
