package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrievePredicateUseCase {
    fun exists(id: ThingId): Boolean
    // legacy methods:
    fun findAll(pageable: Pageable): Page<PredicateRepresentation>
    fun findById(id: ThingId): Optional<PredicateRepresentation>
    fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable): Page<PredicateRepresentation>
}
