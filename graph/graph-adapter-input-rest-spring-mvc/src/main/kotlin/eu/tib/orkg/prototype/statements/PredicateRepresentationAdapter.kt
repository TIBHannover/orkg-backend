package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.statements.api.PredicateRepresentation
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import java.util.*
import org.springframework.data.domain.Page

interface PredicateRepresentationAdapter {

    fun Optional<Predicate>.mapToPredicateRepresentation(): Optional<PredicateRepresentation> =
        map { it.toPredicateRepresentation() }

    fun Page<Predicate>.mapToPredicateRepresentation(): Page<PredicateRepresentation> =
        map { it.toPredicateRepresentation() }

    fun Predicate.toPredicateRepresentation(): PredicateRepresentation =
        PredicateRepresentation(id, label, description, createdAt, createdBy)
}
