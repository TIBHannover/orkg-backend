package org.orkg.common

import java.util.*

data class ObservatoryId(val value: UUID) {

    constructor(s: String) : this(UUID.fromString(s))

    companion object {
        fun createUnknownObservatory() = ObservatoryId(UUID(0, 0))
    }

    override fun toString() = value.toString()
}
