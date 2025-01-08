package org.orkg.testing.configuration

import java.time.Clock
import org.orkg.common.testing.fixtures.fixedClock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

/** Spring configuration for tests that provides a fixed clock. */
@TestConfiguration
class FixedClockConfig {
    @Bean
    fun clock(): Clock = fixedClock
}
