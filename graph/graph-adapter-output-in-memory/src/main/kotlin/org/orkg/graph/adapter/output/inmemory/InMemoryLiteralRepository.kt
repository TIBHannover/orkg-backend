package org.orkg.graph.adapter.output.inmemory

import java.time.OffsetDateTime
import java.util.*
import kotlin.jvm.optionals.getOrNull
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.SearchString
import org.orkg.graph.output.LiteralRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

class InMemoryLiteralRepository(inMemoryGraph: InMemoryGraph) :
    AdaptedInMemoryRepository<ThingId, Literal>(compareBy(Literal::createdAt)), LiteralRepository {

    override val entities: InMemoryEntityAdapter<ThingId, Literal> = object : InMemoryEntityAdapter<ThingId, Literal> {
        override val keys: Collection<ThingId> get() = inMemoryGraph.findAllLiterals().map { it.id }
        override val values: MutableCollection<Literal> get() = inMemoryGraph.findAllLiterals().toMutableSet()

        override fun remove(key: ThingId): Literal? = inMemoryGraph.remove(key).takeIf { it is Literal } as? Literal
        override fun clear() = inMemoryGraph.findAllLiterals().forEach(inMemoryGraph::remove)

        override fun contains(id: ThingId) = inMemoryGraph.findLiteralById(id).isPresent
        override fun get(key: ThingId): Literal? = inMemoryGraph.findLiteralById(key).getOrNull()
        override fun set(key: ThingId, value: Literal): Literal? =
            inMemoryGraph.findLiteralById(key).also { inMemoryGraph.add(value) }.orElse(null)
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

    override fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable) =
        entities.values
            .filter { it.label.matches(labelSearchString) }
            .sortedWith(compareBy { it.label.length })
            .paged(pageable)

    override fun findAllWithFilters(
        createdBy: ContributorId?,
        createdAt: OffsetDateTime?,
        pageable: Pageable
    ): Page<Literal> = findAllFilteredAndPaged(pageable, pageable.sort.literalComparator) {
        (createdBy == null || createdBy == it.createdBy) &&
            (createdAt == null || createdAt == it.createdAt)
    }
}
