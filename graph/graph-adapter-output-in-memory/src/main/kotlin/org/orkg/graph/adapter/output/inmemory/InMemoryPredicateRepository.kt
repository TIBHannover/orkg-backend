package org.orkg.graph.adapter.output.inmemory

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.SearchString
import org.orkg.graph.output.PredicateRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.time.OffsetDateTime
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class InMemoryPredicateRepository(val inMemoryGraph: InMemoryGraph) :
    InMemoryRepository<ThingId, Predicate>(compareBy(Predicate::createdAt)),
    PredicateRepository {
    override val entities: InMemoryEntityAdapter<ThingId, Predicate> =
        object : InMemoryEntityAdapter<ThingId, Predicate> {
            override val keys: Collection<ThingId> get() = inMemoryGraph.findAllPredicates().map { it.id }
            override val values: MutableCollection<Predicate> get() = inMemoryGraph.findAllPredicates().toMutableSet()

            override fun remove(key: ThingId): Predicate? = get(key)?.also { inMemoryGraph.delete(it.id) }

            override fun clear() = inMemoryGraph.findAllPredicates().forEach(inMemoryGraph::delete)

            override fun get(key: ThingId): Predicate? = inMemoryGraph.findPredicateById(key).getOrNull()

            override fun set(key: ThingId, value: Predicate): Predicate? =
                get(key).also { inMemoryGraph.add(value) }
        }

    override fun findById(id: ThingId) = Optional.ofNullable(entities[id])

    override fun findAll(pageable: Pageable): Page<Predicate> =
        findAll(
            pageable = pageable,
            label = null,
            createdBy = null,
            createdAtStart = null,
            createdAtEnd = null
        )

    override fun findAll(
        pageable: Pageable,
        label: SearchString?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
    ): Page<Predicate> =
        findAllFilteredAndPaged(
            pageable = pageable,
            comparator = if (label != null) {
                compareBy { it.label.length }
            } else {
                pageable.withDefaultSort { Sort.by("created_at") }.sort.predicateComparator
            },
            predicate = {
                (label == null || it.label.matches(label)) &&
                    (createdBy == null || it.createdBy == createdBy) &&
                    (createdAtStart == null || it.createdAt >= createdAtStart) &&
                    (createdAtEnd == null || it.createdAt <= createdAtEnd)
            }
        )

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
        while (id in entities) {
            id = ThingId("P${++count}")
        }
        return id
    }

    // this method does not check for rosetta stone statement usage
    override fun isInUse(id: ThingId): Boolean =
        inMemoryGraph.findAllStatements().any { it.`object`.id == id || it.predicate.id == id }
}
