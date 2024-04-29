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
    fun exists(id: ThingId): Boolean

    // legacy methods:
    fun nextIdentity(): ThingId
    fun save(literal: Literal)
    fun deleteAll()
    fun findAll(pageable: Pageable): Page<Literal>
    fun findById(id: ThingId): Optional<Literal>
    fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable): Page<Literal>
    fun findAllWithFilters(
        createdBy: ContributorId? = null,
        createdAt: OffsetDateTime? = null,
        pageable: Pageable
    ): Page<Literal>
}
