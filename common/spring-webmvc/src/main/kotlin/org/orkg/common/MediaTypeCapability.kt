package org.orkg.common

data class MediaTypeCapability<out T : Any>(
    val parameterName: String,
    val default: T,
    private val parser: (String) -> T,
) {
    fun parseValue(string: String): T = parser(string)
}
