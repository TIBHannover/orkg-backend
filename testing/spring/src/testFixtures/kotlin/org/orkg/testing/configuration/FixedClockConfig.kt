package org.orkg.testing.configuration

import org.orkg.common.testing.fixtures.fixedClock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.time.Clock

/** Spring configuration for tests that provides a fixed clock. */
@TestConfiguration
class FixedClockConfig {
    @Bean
    fun clock(): Clock = fixedClock
}
