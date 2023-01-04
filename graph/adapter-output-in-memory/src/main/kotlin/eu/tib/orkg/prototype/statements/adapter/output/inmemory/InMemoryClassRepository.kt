package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

class InMemoryClassRepository : InMemoryRepository<ClassId, Class>(
    compareBy(Class::createdAt)
), ClassRepository {
    override fun save(c: Class) {
        entities[c.id!!] = c
    }

    override fun findByClassId(id: ClassId?): Optional<Class> = Optional.ofNullable(entities[id!!])

    override fun findAllByClassId(id: Iterable<ClassId>, pageable: Pageable): Page<Class> {
        TODO("Not yet implemented")
    }

    override fun findAllByLabel(label: String): Iterable<Class> {
        TODO("Not yet implemented")
    }

    override fun findAllByLabel(label: String, pageable: Pageable): Page<Class> {
        TODO("Not yet implemented")
    }

    override fun findAllByLabelMatchesRegex(label: String): Iterable<Class> {
        TODO("Not yet implemented")
    }

    override fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Class> {
        TODO("Not yet implemented")
    }

    override fun findAllByLabelContaining(part: String): Iterable<Class> {
        TODO("Not yet implemented")
    }

    override fun findByUri(uri: String): Optional<Class> {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        entities.clear()
    }

    override fun nextIdentity(): ClassId {
        TODO("Not yet implemented")
    }

    override fun existsAll(ids: Set<ClassId>): Boolean =
        if (ids.isNotEmpty()) entities.keys.containsAll(ids) else false
}
