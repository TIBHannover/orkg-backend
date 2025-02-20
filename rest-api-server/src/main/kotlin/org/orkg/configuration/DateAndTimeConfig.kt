package org.orkg.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class DateAndTimeConfig {
    @Bean
    fun clock(): Clock = Clock.systemDefaultZone()
}
