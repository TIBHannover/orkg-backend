package org.orkg.statistics.domain

class SimpleMetric(
    override val name: String,
    override val description: String,
    override val group: String = Metric.DEFAULT_GROUP,
    private val supplier: () -> Number
) : Metric {
    override fun value(): Number = supplier()
}
