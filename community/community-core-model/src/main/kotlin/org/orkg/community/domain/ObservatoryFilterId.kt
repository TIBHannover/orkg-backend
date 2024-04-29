package org.orkg.community.domain

import java.util.*

data class ObservatoryFilterId(val value: UUID) {

    constructor(s: String) : this(UUID.fromString(s))

    override fun toString() = value.toString()
}
