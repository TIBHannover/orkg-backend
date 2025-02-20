package org.orkg.statistics.domain

import kotlin.reflect.KClass

class ParameterSpec<out T : Any>(
    val name: String,
    val description: String,
    val type: KClass<out T>,
    private val parser: (String) -> T,
) {
    fun parse(value: String): T = parser(value)
}
