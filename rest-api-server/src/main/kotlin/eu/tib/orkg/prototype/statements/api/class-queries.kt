package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveClassUseCase {
    fun findAll(): Sequence<Class>
    // legacy methods:
    fun exists(id: ClassId): Boolean
    fun findAll(pageable: Pageable): Page<Class>
    fun findById(id: ClassId): Optional<Class>
    fun findAllByLabel(label: String): Iterable<Class>
    fun findAllByLabel(pageable: Pageable, label: String): Page<Class>
    fun findAllByLabelContaining(part: String): Iterable<Class>
    fun findAllByLabelContaining(pageable: Pageable, part: String): Page<Class>
}
