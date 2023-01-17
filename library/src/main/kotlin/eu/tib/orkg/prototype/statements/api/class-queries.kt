package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveClassUseCase {
    fun exists(id: ClassId): Boolean
    // legacy methods:
    fun findAll(pageable: Pageable): Page<ClassRepresentation>
    fun findAllById(ids: Iterable<ClassId>, pageable: Pageable): Page<ClassRepresentation>
    fun findById(id: ClassId): Optional<ClassRepresentation>
    fun findAllByLabel(label: String): Iterable<ClassRepresentation>
    fun findAllByLabel(pageable: Pageable, label: String): Page<ClassRepresentation>
    fun findAllByLabelContaining(part: String): Iterable<ClassRepresentation>
    fun findAllByLabelContaining(pageable: Pageable, part: String): Page<ClassRepresentation>
}
