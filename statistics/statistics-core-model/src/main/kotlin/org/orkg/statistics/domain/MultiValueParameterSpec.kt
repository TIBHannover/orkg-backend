package org.orkg.statistics.domain

import kotlin.reflect.KClass

class MultiValueParameterSpec<out T : Any>(
    override val name: String,
    override val description: String,
    val type: KClass<out T>,
    val values: List<T> = emptyList(),
    private val parser: (String) -> T,
) : ParameterSpec<List<T>> {
    override fun parse(value: List<String>): List<T> = value.map(parser)
}
