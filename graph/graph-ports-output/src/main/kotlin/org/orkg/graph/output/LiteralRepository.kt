package org.orkg.graph.output

import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.SearchString
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface LiteralRepository {
    fun existsById(id: ThingId): Boolean
    fun nextIdentity(): ThingId
    fun save(literal: Literal)
    fun deleteAll()
    fun findById(id: ThingId): Optional<Literal>
    fun findAll(
        pageable: Pageable,
        label: SearchString? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null
    ): Page<Literal>
}
