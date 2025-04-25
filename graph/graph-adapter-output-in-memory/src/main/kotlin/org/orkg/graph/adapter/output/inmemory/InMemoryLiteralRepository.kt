package org.orkg.graph.adapter.output.inmemory

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.withDefaultSort
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.SearchString
import org.orkg.graph.output.LiteralRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.time.OffsetDateTime
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class InMemoryLiteralRepository(inMemoryGraph: InMemoryGraph) :
    InMemoryRepository<ThingId, Literal>(compareBy(Literal::createdAt)),
    LiteralRepository {
    override val entities: InMemoryEntityAdapter<ThingId, Literal> =
        object : InMemoryEntityAdapter<ThingId, Literal> {
            override val keys: Collection<ThingId> get() = inMemoryGraph.findAllLiterals().map { it.id }
            override val values: MutableCollection<Literal> get() = inMemoryGraph.findAllLiterals().toMutableSet()

            override fun remove(key: ThingId): Literal? = get(key)?.also { inMemoryGraph.delete(it.id) }

            override fun clear() = inMemoryGraph.findAllLiterals().forEach(inMemoryGraph::delete)

            override fun get(key: ThingId): Literal? = inMemoryGraph.findLiteralById(key).getOrNull()

            override fun set(key: ThingId, value: Literal): Literal? =
                get(key).also { inMemoryGraph.add(value) }
        }

    override fun nextIdentity(): ThingId {
        var count = entities.size.toLong()
        var id = ThingId("L$count")
        while (id in entities) {
            id = ThingId("L${++count}")
        }
        return id
    }

    override fun save(literal: Literal) {
        entities[literal.id] = literal
    }

    override fun deleteAll() {
        entities.clear()
    }

    override fun findById(id: ThingId) = Optional.ofNullable(entities[id])

    override fun findAll(pageable: Pageable): Page<Literal> =
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
    ): Page<Literal> =
        findAllFilteredAndPaged(
            pageable = pageable,
            comparator = if (label != null) {
                compareBy { it.label.length }
            } else {
                pageable.withDefaultSort { Sort.by("created_at") }.sort.literalComparator
            },
            predicate = {
                (label == null || it.label.matches(label)) &&
                    (createdBy == null || it.createdBy == createdBy) &&
                    (createdAtStart == null || it.createdAt >= createdAtStart) &&
                    (createdAtEnd == null || it.createdAt <= createdAtEnd)
            }
        )
}
