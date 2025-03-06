package org.orkg.statistics.domain

import org.orkg.common.exceptions.UnknownParameter
import org.springframework.util.MultiValueMap

open class SimpleMetric(
    override val name: String,
    override val description: String,
    override val group: String = Metric.DEFAULT_GROUP,
    override val parameterSpecs: Map<String, ParameterSpec<Any>> = emptyMap(),
    private val supplier: (parameters: ParameterMap) -> Number,
) : Metric {
    override fun value(parameters: MultiValueMap<String, String>): Number {
        val unknownParameters = parameters.keys - parameterSpecs.keys
        if (unknownParameters.isNotEmpty()) {
            throw UnknownParameter(unknownParameters.first())
        }
        val parameterMap = ParameterMap()
        parameters.forEach { (key, values) ->
            val spec = parameterSpecs[key]!!
            parameterMap[spec] = spec.parse(values)
        }
        return supplier(parameterMap)
    }
}
