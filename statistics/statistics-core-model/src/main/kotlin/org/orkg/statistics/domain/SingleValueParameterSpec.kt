package org.orkg.statistics.domain

import kotlin.reflect.KClass

class SingleValueParameterSpec<out T : Any>(
    override val name: String,
    override val description: String,
    val type: KClass<out T>,
    val values: List<T> = emptyList(),
    private val parser: (String) -> T,
) : ParameterSpec<T> {
    override fun parse(value: List<String>): T =
        parser(value.singleOrNull() ?: throw TooManyParameterValues(name))
}
