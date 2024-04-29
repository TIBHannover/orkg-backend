package org.orkg.statistics.domain

import java.time.Clock
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class CachedMetric(
    override val name: String,
    override val description: String,
    override val group: String = Metric.DEFAULT_GROUP,
    private val expireAfter: Duration = 5.minutes,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val supplier: () -> Number
) : Metric {
    private lateinit var value: Number
    private var expiresAt: Instant = Instant.MIN

    override fun value(): Number {
        val now = Instant.now(clock)
        if (now.isAfter(expiresAt)) {
            value = supplier()
            expiresAt = now.plus(expireAfter.toJavaDuration())
        }
        return value
    }
}
