package org.orkg.statistics.domain

interface Metric {
    val name: String
    val description: String
    val group: String
    val parameterSpecs: Map<String, ParameterSpec<*>>

    fun value(parameters: Map<String, String>): Number

    companion object {
        const val DEFAULT_GROUP = "generic"
    }
}
