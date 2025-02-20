package org.orkg.community.domain

import java.util.UUID

data class ConferenceSeriesId(val value: UUID) {
    constructor(s: String) : this(UUID.fromString(s))

    override fun toString() = value.toString()
}
