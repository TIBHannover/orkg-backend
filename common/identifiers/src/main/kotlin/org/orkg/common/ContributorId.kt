package org.orkg.common

import java.util.UUID

data class ContributorId(val value: UUID) {
    constructor(s: String) : this(UUID.fromString(s))

    companion object {
        val UNKNOWN: ContributorId = ContributorId(UUID(0, 0))
        val SYSTEM: ContributorId = ContributorId(UUID(-1, -1))
    }

    override fun toString() = value.toString()
}
