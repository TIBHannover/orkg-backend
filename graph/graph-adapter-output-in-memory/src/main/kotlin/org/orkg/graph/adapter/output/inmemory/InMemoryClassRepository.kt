package org.orkg.graph.adapter.output.inmemory

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.toOptional
import org.orkg.graph.output.ClassRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.time.OffsetDateTime
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class InMemoryClassRepository(inMemoryGraph: InMemoryGraph) :
    InMemoryRepository<ThingId, Class>(compareBy(Class::createdAt)),
    ClassRepository {
    override val entities: InMemoryEntityAdapter<ThingId, Class> =
        object : InMemoryEntityAdapter<ThingId, Class> {
            override val keys: Collection<ThingId> get() = inMemoryGraph.findAllClasses().map { it.id }
            override val values: MutableCollection<Class> get() = inMemoryGraph.findAllClasses().toMutableSet()

            override fun remove(key: ThingId): Class? = get(key)?.also { inMemoryGraph.delete(it.id) }

            override fun clear() = inMemoryGraph.findAllClasses().forEach(inMemoryGraph::delete)

            override fun get(key: ThingId): Class? = inMemoryGraph.findClassById(key).getOrNull()

            override fun set(key: ThingId, value: Class): Class? =
                get(key).also { inMemoryGraph.add(value) }
        }

    override fun save(c: Class) {
        entities[c.id] = c
    }

    override fun findById(id: ThingId): Optional<Class> = Optional.ofNullable(entities[id])

    override fun findAll(pageable: Pageable): Page<Class> =
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
    ): Page<Class> =
        findAllFilteredAndPaged(
            pageable = pageable,
            comparator = if (label != null) {
                compareBy { it.label.length }
            } else {
                pageable.withDefaultSort { Sort.by("created_at") }.sort.classComparator
            },
            predicate = {
                (label == null || it.label.matches(label)) &&
                    (createdBy == null || it.createdBy == createdBy) &&
                    (createdAtStart == null || it.createdAt >= createdAtStart) &&
                    (createdAtEnd == null || it.createdAt <= createdAtEnd)
            }
        )

    @Deprecated("For removal")
    override fun findAllById(id: Iterable<ThingId>, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { id.contains(it.id) }

    override fun findByUri(uri: String) =
        entities.values.firstOrNull { it.uri.toString() == uri }.toOptional()

    override fun deleteAll() {
        entities.clear()
    }

    override fun nextIdentity(): ThingId {
        var count = entities.size.toLong()
        var id = ThingId("C$count")
        while (id in entities) {
            id = ThingId("C${++count}")
        }
        return id
    }

    override fun existsAllById(ids: Set<ThingId>): Boolean =
        if (ids.isNotEmpty()) entities.keys.containsAll(ids) else false
}
