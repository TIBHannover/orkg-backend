package org.orkg.graph.domain

import org.orkg.common.VALID_ID_REGEX
import java.io.Serial
import java.io.Serializable

data class StatementId(val value: String) :
    Comparable<StatementId>,
    Serializable {
    init {
        require(value.isNotBlank()) { "ID must not be blank" }
        require(value.startsWith("S")) { "ID must start with \"S\"" }
        require(value.matches(VALID_ID_REGEX)) { "Must only contain alphanumeric characters, dashes and underscores" }
    }

    constructor(value: Long) : this("S$value") {
        require(value >= 0) { "Value must be greater than or equal to zero" }
    }

    override fun toString() = value

    override fun compareTo(other: StatementId) = value.compareTo(other.value)

    companion object {
        @Serial
        private const val serialVersionUID: Long = 3366478835159789925L
    }
}
