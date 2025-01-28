package org.orkg.statistics.domain

import org.orkg.common.exceptions.UnknownParameter

open class SimpleMetric(
    override val name: String,
    override val description: String,
    override val group: String = Metric.DEFAULT_GROUP,
    override val parameterSpecs: Map<String, ParameterSpec<Any>> = emptyMap(),
    private val supplier: (parameters: ParameterMap) -> Number
) : Metric {
    override fun value(parameters: Map<String, String>): Number {
        val unknownParameters = parameters.keys - parameterSpecs.keys
        if (unknownParameters.isNotEmpty()) {
            throw UnknownParameter(unknownParameters.first())
        }
        val parameterMap = ParameterMap()
        parameters.forEach { (key, value) ->
            val spec = parameterSpecs[key]!!
            parameterMap[spec] = spec.parse(value)
        }
        return supplier(parameterMap)
    }
}
