package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrievePredicateUseCase {
    fun exists(id: PredicateId): Boolean
    // legacy methods:
    fun findAll(pageable: Pageable): Page<PredicateRepresentation>
    fun findById(id: PredicateId?): Optional<PredicateRepresentation>
    fun findAllByLabel(label: String, pageable: Pageable): Page<PredicateRepresentation>
    fun findAllByLabelContaining(part: String, pageable: Pageable): Page<PredicateRepresentation>
}
