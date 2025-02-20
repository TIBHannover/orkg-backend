package org.orkg.statistics.domain

import org.slf4j.LoggerFactory
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager

const val METRICS_NAME_AND_PARAMETERS_TO_VALUE_CACHE = "metrics-name-and-parameters-to-number-cache"

class CachedMetric private constructor(
    override val name: String,
    override val description: String,
    override val group: String,
    override val parameterSpecs: Map<String, ParameterSpec<Any>>,
    private val cache: Cache?,
    supplier: (parameters: ParameterMap) -> Number,
) : SimpleMetric(name, description, group, parameterSpecs, supplier) {
    init {
        if (cache == null) {
            LoggerFactory.getLogger(this::class.java).info("No cache provided for metric $group-$name. Caching will be disabled.")
        }
    }

    override fun value(parameters: Map<String, String>): Number =
        cache?.get(computeKey(parameters)) { super.value(parameters) }
            ?: super.value(parameters)

    private fun computeKey(parameters: Map<String, String>): String =
        "$group-$name:[${parameters.entries.joinToString(",") { (key, value) -> "$key=$value" }}]"

    companion object {
        fun create(
            cacheManager: CacheManager?,
            name: String,
            description: String,
            group: String = Metric.DEFAULT_GROUP,
            parameterSpecs: Map<String, ParameterSpec<Any>> = emptyMap(),
            supplier: (parameters: ParameterMap) -> Number,
        ): Metric =
            CachedMetric(
                name = name,
                description = description,
                group = group,
                parameterSpecs = parameterSpecs,
                cache = cacheManager?.getCache(METRICS_NAME_AND_PARAMETERS_TO_VALUE_CACHE),
                supplier = supplier
            )
    }
}
