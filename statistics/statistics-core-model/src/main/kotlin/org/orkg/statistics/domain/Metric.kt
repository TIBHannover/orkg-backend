package org.orkg.statistics.domain

interface Metric {
    val name: String
    val description: String
    val group: String

    fun value(): Number

    companion object {
        const val DEFAULT_GROUP = "generic"
    }
}
