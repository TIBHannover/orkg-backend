package org.orkg.graph.adapter.output.inmemory

import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.toOptional
import org.orkg.graph.output.ClassRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

class InMemoryClassRepository : InMemoryRepository<ThingId, Class>(
    compareBy(Class::createdAt)
), ClassRepository {
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
        (uri == null || uri == it.uri.toString())
            && (createdBy == null || createdBy == it.createdBy)
            && (createdAt == null || createdAt == it.createdAt)
    }

    override fun deleteAll() {
        entities.clear()
    }

    override fun nextIdentity(): ThingId {
        var count = entities.size.toLong()
        var id = ThingId("C$count")
        while(id in entities) {
            id = ThingId("C${++count}")
        }
        return id
    }

    override fun existsAll(ids: Set<ThingId>): Boolean =
        if (ids.isNotEmpty()) entities.keys.containsAll(ids) else false
}
