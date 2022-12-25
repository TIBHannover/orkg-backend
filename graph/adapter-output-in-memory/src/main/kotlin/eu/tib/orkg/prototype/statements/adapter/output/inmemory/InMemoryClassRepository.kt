package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

class InMemoryClassRepository : ClassRepository {
    private val entities: MutableSet<Class> = mutableSetOf()

    override fun save(c: Class) {
        entities += c
    }

    override fun findByClassId(id: ClassId?): Optional<Class> = Optional.of(entities.single { it.id == id })

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

    override fun existsAll(ids: Set<ClassId>): Boolean {
        TODO("Not yet implemented")
    }

    override fun findAll(pageable: Pageable) = PageImpl(
        entities
            .sortedBy(Class::createdAt)
            .drop(pageable.pageNumber * pageable.pageSize)
            .take(pageable.pageSize),
        PageRequest.of(pageable.pageNumber, pageable.pageSize),
        entities.size.toLong()
    )

    override fun exists(id: ClassId): Boolean {
        TODO("Not yet implemented")
    }
}
