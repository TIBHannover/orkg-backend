package org.orkg.dataimport.domain.csv

import java.io.Serializable
import java.util.UUID

data class CSVID(val value: UUID) : Serializable {
    constructor(s: String) : this(UUID.fromString(s))

    override fun toString() = value.toString()
}
