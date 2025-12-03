package org.orkg.statistics.domain

sealed interface ParameterSpec<out T : Any> {
    val name: String
    val description: String

    fun parse(values: List<String>): T
}
