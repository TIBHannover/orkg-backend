package org.orkg.common

import java.util.*

data class OrganizationId(val value: UUID) {

    constructor(s: String) : this(UUID.fromString(s))

    companion object {
        val UNKNOWN = OrganizationId(UUID(0, 0))
    }

    override fun toString() = value.toString()
}
