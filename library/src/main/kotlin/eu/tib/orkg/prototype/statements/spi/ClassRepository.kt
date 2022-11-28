package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ClassRepository : EntityRepository<Class, ClassId> {
    // legacy methods:
    fun save(c: Class): Class
    fun findByClassId(id: ClassId?): Optional<Class>
    fun findAllByLabel(label: String): Iterable<Class>
    fun findAllByLabel(label: String, pageable: Pageable): Page<Class>
    fun findAllByLabelMatchesRegex(label: String): Iterable<Class>
    fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Class>
    fun findAllByLabelContaining(part: String): Iterable<Class>
    fun findByUri(uri: String): Optional<Class>
    fun deleteAll()
    fun nextIdentity(): ClassId
    fun existsAll(ids: Iterable<ClassId>): Boolean
}
