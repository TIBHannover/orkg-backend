package org.orkg.dataimport.domain.csv

import java.io.Serial
import java.io.Serializable
import java.util.UUID

data class CSVID(val value: UUID) : Serializable {
    constructor(s: String) : this(UUID.fromString(s))

    override fun toString() = value.toString()

    companion object {
        @Serial
        private const val serialVersionUID: Long = 7895703194322646576L
    }
}
