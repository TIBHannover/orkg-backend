package org.orkg.graph.output

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.SearchString
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.Optional

interface PredicateRepository : EntityRepository<Predicate, ThingId> {
    fun findById(id: ThingId): Optional<Predicate>

    fun findAll(
        pageable: Pageable,
        label: SearchString? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null,
    ): Page<Predicate>

    fun deleteById(id: ThingId)

    fun deleteAll()

    fun save(predicate: Predicate)

    fun nextIdentity(): ThingId

    fun isInUse(id: ThingId): Boolean
}
