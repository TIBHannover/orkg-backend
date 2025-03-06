package org.orkg.statistics.adapter.input.rest.configuration

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.orkg.statistics.domain.Metric
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.LinkedMultiValueMap

private val emptyMultiMap = LinkedMultiValueMap<String, String>()

@Configuration
class ActuatorConfiguration {
    @Bean
    fun registerMetrics(registry: MeterRegistry, metrics: List<Metric>): List<Gauge> =
        metrics.map { metric ->
            Gauge.builder("""orkg-statistics-${metric.group}-${metric.name}""") { metric.value(emptyMultiMap) }
                .description(metric.description)
                .register(registry)
        }
}
