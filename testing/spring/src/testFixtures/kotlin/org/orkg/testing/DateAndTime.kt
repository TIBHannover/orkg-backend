package org.orkg.testing

import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

/** An arbitrary fixed timestamp for use in tests. */
val testTimestamp: OffsetDateTime = OffsetDateTime.parse("2023-11-30T09:25:14.049085776+01:00")

/** An implementation of [java.time.Clock] which returns the fixed timestamp defined by [testTimestamp]. */
val fixedClock: Clock = Clock.fixed(Instant.from(testTimestamp), ZoneId.from(testTimestamp))

/** Spring configuration for tests that provides a fixed clock. */
@TestConfiguration
class FixedClockConfig {
    @Bean
    fun clock(): Clock = fixedClock
}
