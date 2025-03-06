package org.orkg.statistics.domain

import org.springframework.util.MultiValueMap

interface Metric {
    val name: String
    val description: String
    val group: String
    val parameterSpecs: Map<String, ParameterSpec<*>>

    fun value(parameters: MultiValueMap<String, String>): Number

    companion object {
        const val DEFAULT_GROUP = "generic"
    }
}
