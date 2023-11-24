package org.orkg.graph.adapter.output.inmemory

import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.SearchString
import org.orkg.graph.output.PredicateRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

class InMemoryPredicateRepository : InMemoryRepository<ThingId, Predicate>(
    compareBy(Predicate::createdAt)
), PredicateRepository {
    override fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable) =
        entities.values
            .filter { it.label.matches(labelSearchString) }
            .sortedWith(compareBy { it.label.length })
            .paged(pageable)

    override fun findById(id: ThingId) = Optional.ofNullable(entities[id])

    override fun deleteById(id: ThingId) {
        entities.remove(id)
    }

    override fun deleteAll() {
        entities.clear()
    }

    override fun save(predicate: Predicate) {
        entities[predicate.id] = predicate
    }

    override fun nextIdentity(): ThingId {
        var count = entities.size.toLong()
        var id = ThingId("P$count")
        while(id in entities) {
            id = ThingId("P${++count}")
        }
        return id
    }

    override fun findAllWithFilters(
        createdBy: ContributorId?,
        createdAt: OffsetDateTime?,
        pageable: Pageable
    ): Page<Predicate> = findAllFilteredAndPaged(pageable, pageable.sort.predicateComparator) {
        (createdBy == null || createdBy == it.createdBy)
            && (createdAt == null || createdAt == it.createdAt)
    }
}
