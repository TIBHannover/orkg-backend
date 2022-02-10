package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrievePredicateUseCase {
    // legacy methods:
    fun findAll(pageable: Pageable): Page<Predicate>
    fun findById(id: PredicateId?): Optional<Predicate>
    fun findAllByLabel(label: String, pageable: Pageable): Page<Predicate>
    fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Predicate>
}
