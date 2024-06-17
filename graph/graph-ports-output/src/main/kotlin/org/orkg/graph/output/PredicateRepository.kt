package org.orkg.graph.output

import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.SearchString
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PredicateRepository : EntityRepository<Predicate, ThingId> {
    // legacy methods:
    fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable): Page<Predicate>
    // TODO: Work-around for https://jira.spring.io/browse/DATAGRAPH-1200. Replace with IgnoreCase or ContainsIgnoreCase when fixed.
    fun findById(id: ThingId): Optional<Predicate>
    fun deleteById(id: ThingId)
    fun deleteAll()
    fun save(predicate: Predicate)
    fun nextIdentity(): ThingId
    fun findAllWithFilters(
        createdBy: ContributorId? = null,
        createdAt: OffsetDateTime? = null,
        pageable: Pageable
    ): Page<Predicate>
    fun isInUse(id: ThingId): Boolean
}
