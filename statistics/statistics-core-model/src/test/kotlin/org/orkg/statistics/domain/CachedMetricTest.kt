package org.orkg.statistics.domain

import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CachedMetricTest {
    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @Test
    fun `Given a cached metric, retrieving its value should be cached`() {
        val mockMetricSupplier: () -> Number = mockk()
        val clock: Clock = mockk()
        val metric = CachedMetric(
            name = "test",
            group = "test",
            description = "test description",
            supplier = mockMetricSupplier,
            expireAfter = 10.minutes,
            clock = clock
        )
        val now = Instant.parse("2007-12-03T10:15:30.00Z")

        every { mockMetricSupplier() } returns 5 andThen 10 andThenAnswer {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }

        every { clock.instant() } returns now

        // Obtain value from metric
        metric.value() shouldBe 5

        verify(exactly = 1) { clock.instant() }
        // Verify the loading happened
        verify(exactly = 1) { mockMetricSupplier() }

        // Obtain value from metric several times
        metric.value() shouldBe 5
        // Advance time by 5 minutes
        every { clock.instant() } returns now.plus(5.minutes.toJavaDuration())
        metric.value() shouldBe 5

        verify(exactly = 3) { clock.instant() }

        // Advance time by 10 minutes
        every { clock.instant() } returns now.plus(15.minutes.toJavaDuration())

        // Obtain value from metric after cache expired
        metric.value() shouldBe 10

        verify(exactly = 4) { clock.instant() }
        // Verify the loading happened
        verify(exactly = 2) { mockMetricSupplier() }

        // Advance time by 5 minutes
        every { clock.instant() } returns now.plus(20.minutes.toJavaDuration())

        // Obtain value from metric several times
        metric.value() shouldBe 10
        metric.value() shouldBe 10

        verify(exactly = 6) { clock.instant() }

        confirmVerified(mockMetricSupplier, clock)
    }
}
