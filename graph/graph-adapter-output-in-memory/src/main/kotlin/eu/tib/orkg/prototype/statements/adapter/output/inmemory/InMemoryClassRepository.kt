package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.toOptional
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import java.util.*
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
