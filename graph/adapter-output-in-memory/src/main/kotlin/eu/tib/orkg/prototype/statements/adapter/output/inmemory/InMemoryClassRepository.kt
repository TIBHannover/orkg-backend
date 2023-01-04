package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.toOptional
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import java.util.*
import org.springframework.data.domain.Pageable

class InMemoryClassRepository : InMemoryRepository<ClassId, Class>(
    compareBy(Class::createdAt)
), ClassRepository {
    override fun save(c: Class) {
        entities[c.id!!] = c
    }

    override fun findByClassId(id: ClassId?): Optional<Class> = Optional.ofNullable(entities[id!!])

    override fun findAllByClassId(id: Iterable<ClassId>, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { id.contains(it.id!!) }

    override fun findAllByLabel(label: String) = entities.values.filter { it.label == label }

    override fun findAllByLabel(label: String, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { it.label == label }

    override fun findAllByLabelMatchesRegex(label: String) =
        entities.values.filter { it.label.matches(Regex(label)) }

    override fun findAllByLabelMatchesRegex(label: String, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { it.label.matches(Regex(label)) }

    override fun findAllByLabelContaining(part: String) =
        entities.values.filter { it.label.contains(part) }

    override fun findByUri(uri: String) =
        entities.values.firstOrNull { it.uri.toString() == uri }.toOptional()

    override fun deleteAll() {
        entities.clear()
    }

    override fun nextIdentity(): ClassId {
        var id = ClassId(entities.size.toLong())
        while(id in entities) {
            id = ClassId(id.value.toLong() + 1)
        }
        return id
    }

    override fun existsAll(ids: Set<ClassId>): Boolean =
        if (ids.isNotEmpty()) entities.keys.containsAll(ids) else false
}
