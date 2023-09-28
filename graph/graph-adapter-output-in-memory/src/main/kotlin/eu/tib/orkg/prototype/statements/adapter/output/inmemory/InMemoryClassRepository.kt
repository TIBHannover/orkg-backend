package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.VisibilityFilter
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.statements.domain.model.toOptional
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

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
