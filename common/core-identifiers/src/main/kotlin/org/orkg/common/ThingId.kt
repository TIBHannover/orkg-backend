package org.orkg.common

import java.io.Serial
import java.io.Serializable

data class ThingId(val value: String) :
    Comparable<ThingId>,
    Serializable {
    init {
        require(value.isNotBlank()) { "ID must not be blank" }
        require(value.matches(VALID_ID_REGEX)) { "Must only contain alphanumeric characters, dashes and underscores" }
    }

    override fun toString() = value

    override fun compareTo(other: ThingId) = value.compareTo(other.value)

    companion object {
        @Serial
        private const val serialVersionUID: Long = -3190585433188613056L
    }
}

@Suppress("RegExpSimplifiable")
val VALID_ID_REGEX: Regex = """^[a-zA-Z0-9:_-]+$""".toRegex()
