package org.orkg.common

data class ThingId(val value: String) : Comparable<ThingId> {

    init {
        require(value.isNotBlank()) { "ID must not be blank" }
        require(value.matches(VALID_ID_REGEX)) { "Must only contain alphanumeric characters, dashes and underscores" }
    }

    override fun toString() = value

    override fun compareTo(other: ThingId) = value.compareTo(other.value)
}

@Suppress("RegExpSimplifiable")
val VALID_ID_REGEX: Regex = """^[a-zA-Z0-9:_-]+$""".toRegex()
