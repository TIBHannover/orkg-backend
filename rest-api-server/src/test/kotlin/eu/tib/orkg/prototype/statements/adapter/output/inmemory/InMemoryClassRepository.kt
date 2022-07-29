package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

class InMemoryClassRepository : ClassRepository {
    override fun findAll(): Sequence<Class> {
        TODO("Not yet implemented")
    }

    override fun findAll(pageable: Pageable): Page<Class> {
        TODO("Not yet implemented")
    }

    override fun save(c: Class): Class {
        TODO("Not yet implemented")
    }

    override fun findByClassId(id: ClassId?): Optional<Class> {
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
        TODO("Not yet implemented")
    }

    override fun nextIdentity(): ClassId {
        TODO("Not yet implemented")
    }
}
