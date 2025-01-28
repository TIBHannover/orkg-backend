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
    supplier: (parameters: ParameterMap) -> Number
) : SimpleMetric(name, description, group, emptyMap(), supplier) {
    private lateinit var value: Number
    private var expiresAt: Instant = Instant.MIN

    override fun value(parameters: Map<String, String>): Number {
        val now = Instant.now(clock)
        if (now.isAfter(expiresAt)) {
            value = super.value(parameters)
            expiresAt = now.plus(expireAfter.toJavaDuration())
        }
        return value
    }
}
