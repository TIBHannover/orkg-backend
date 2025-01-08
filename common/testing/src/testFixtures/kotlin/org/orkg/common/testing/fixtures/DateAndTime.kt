package org.orkg.common.testing.fixtures

import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

/** An arbitrary fixed timestamp for use in tests. */
val testTimestamp: OffsetDateTime = OffsetDateTime.parse("2023-11-30T09:25:14.049085776+01:00")

/** An implementation of [java.time.Clock] which returns the fixed timestamp defined by [testTimestamp]. */
val fixedClock: Clock = Clock.fixed(Instant.from(testTimestamp), ZoneId.from(testTimestamp))
