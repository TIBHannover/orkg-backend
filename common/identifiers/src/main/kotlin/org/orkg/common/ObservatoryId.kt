package org.orkg.common

import java.util.UUID

data class ObservatoryId(val value: UUID) {
    constructor(s: String) : this(UUID.fromString(s))

    companion object {
        val UNKNOWN = ObservatoryId(UUID(0, 0))
    }

    override fun toString() = value.toString()
}
