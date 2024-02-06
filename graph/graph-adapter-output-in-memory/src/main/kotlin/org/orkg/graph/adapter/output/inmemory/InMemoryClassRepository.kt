package org.orkg.graph.adapter.output.inmemory

import java.time.OffsetDateTime
import java.util.*
import kotlin.jvm.optionals.getOrNull
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.toOptional
import org.orkg.graph.output.ClassRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

class InMemoryClassRepository(inMemoryGraph: InMemoryGraph) :
    AdaptedInMemoryRepository<ThingId, Class>(compareBy(Class::createdAt)), ClassRepository {

    override val entities: InMemoryEntityAdapter<ThingId, Class> = object : InMemoryEntityAdapter<ThingId, Class> {
        override val keys: Collection<ThingId> get() = inMemoryGraph.findAllClasses().map { it.id }
        override val values: MutableCollection<Class> get() = inMemoryGraph.findAllClasses().toMutableSet()

        override fun remove(key: ThingId): Class? = inMemoryGraph.remove(key).takeIf { it is Class } as? Class
        override fun clear() = inMemoryGraph.findAllClasses().forEach(inMemoryGraph::remove)

        override fun contains(id: ThingId) = inMemoryGraph.findClassById(id).isPresent
        override fun get(key: ThingId): Class? = inMemoryGraph.findClassById(key).getOrNull()
        override fun set(key: ThingId, value: Class): Class? =
            inMemoryGraph.findClassById(key).also { inMemoryGraph.add(value) }.orElse(null)
    }

    override fun save(c: Class) {
        entities[c.id] = c
    }

    override fun findById(id: ThingId): Optional<Class> = Optional.ofNullable(entities[id])

    override fun findAllById(id: Iterable<ThingId>, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { id.contains(it.id) }

    override fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable) =
        entities.values
            .filter { it.label.matches(labelSearchString) }
            .sortedWith(compareBy { it.label.length })
            .paged(pageable)

    override fun findByUri(uri: String) =
        entities.values.firstOrNull { it.uri.toString() == uri }.toOptional()

    override fun findAllWithFilters(
        uri: String?,
        createdBy: ContributorId?,
        createdAt: OffsetDateTime?,
        pageable: Pageable
    ): Page<Class> = findAllFilteredAndPaged(pageable, pageable.sort.classComparator) {
        (uri == null || uri == it.uri.toString()) &&
            (createdBy == null || createdBy == it.createdBy) &&
            (createdAt == null || createdAt == it.createdAt)
    }

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

    override fun existsAll(ids: Set<ThingId>): Boolean =
        if (ids.isNotEmpty()) entities.keys.containsAll(ids) else false
}
