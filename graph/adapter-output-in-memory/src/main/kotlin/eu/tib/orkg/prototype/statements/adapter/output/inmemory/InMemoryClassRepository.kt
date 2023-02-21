package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.toOptional
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

class InMemoryClassRepository : InMemoryRepository<ThingId, Class>(
    compareBy(Class::createdAt)
), ClassRepository {
    override fun save(c: Class) {
        entities[c.id] = c
    }

    override fun findByClassId(id: ThingId?): Optional<Class> = Optional.ofNullable(entities[id!!])

    override fun findAllByClassId(id: Iterable<ThingId>, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { id.contains(it.id) }

    override fun findAllByLabel(label: String) = entities.values.filter { it.label == label }

    override fun findAllByLabel(label: String, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { it.label == label }

    override fun findAllByLabelMatchesRegex(label: String): List<Class> {
        val regex = Regex(label)
        return entities.values.filter { it.label.matches(regex) }
    }

    override fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Class> {
        val regex = Regex(label)
        return findAllFilteredAndPaged(pageable) { it.label.matches(regex) }
    }

    override fun findAllByLabelContaining(part: String) =
        entities.values.filter { it.label.contains(part) }

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
