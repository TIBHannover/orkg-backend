package org.orkg.common

import java.io.Serial
import java.io.Serializable
import java.util.UUID

data class ContributorId(val value: UUID) : Serializable {
    constructor(s: String) : this(UUID.fromString(s))

    companion object {
        @Serial
        private const val serialVersionUID: Long = -3253374151622189454L

        val UNKNOWN: ContributorId = ContributorId(UUID(0, 0))
        val SYSTEM: ContributorId = ContributorId(UUID(-1, -1))
    }

    override fun toString() = value.toString()
}
