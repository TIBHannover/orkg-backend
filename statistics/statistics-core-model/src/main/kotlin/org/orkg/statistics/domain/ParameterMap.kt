package org.orkg.statistics.domain

class ParameterMap {
    private val parameters = mutableMapOf<ParameterSpec<*>, Any>()

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(spec: ParameterSpec<T>): T? = parameters[spec] as? T

    operator fun <T : Any> set(spec: ParameterSpec<T>, value: T) {
        parameters[spec] = value
    }
}
