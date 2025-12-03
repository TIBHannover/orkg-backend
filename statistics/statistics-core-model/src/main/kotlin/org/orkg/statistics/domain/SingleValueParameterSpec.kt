package org.orkg.statistics.domain

import kotlin.reflect.KClass

class SingleValueParameterSpec<out T : Any>(
    override val name: String,
    override val description: String,
    val type: KClass<out T>,
    val values: List<T> = emptyList(),
    private val parser: (String) -> T,
) : ParameterSpec<T> {
    override fun parse(values: List<String>): T {
        val value = values.singleOrNull() ?: throw TooManyParameterValues(name)
        try {
            return parser(value)
        } catch (e: Throwable) {
            throw InvalidParameterValue(name, value, e)
        }
    }
}
