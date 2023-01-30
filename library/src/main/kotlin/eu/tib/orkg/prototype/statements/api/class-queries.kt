package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveClassUseCase {
    fun exists(id: ThingId): Boolean
    // legacy methods:
    fun findAll(pageable: Pageable): Page<ClassRepresentation>
    fun findAllById(ids: Iterable<ThingId>, pageable: Pageable): Page<ClassRepresentation>
    fun findById(id: ThingId): Optional<ClassRepresentation>
    fun findAllByLabel(label: String): Iterable<ClassRepresentation>
    fun findAllByLabel(pageable: Pageable, label: String): Page<ClassRepresentation>
    fun findAllByLabelContaining(part: String): Iterable<ClassRepresentation>
    fun findAllByLabelContaining(pageable: Pageable, part: String): Page<ClassRepresentation>
}
