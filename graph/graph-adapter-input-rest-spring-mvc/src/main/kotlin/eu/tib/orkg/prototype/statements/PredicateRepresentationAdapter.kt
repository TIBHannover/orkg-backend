package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.PredicateRepresentation
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page

interface PredicateRepresentationAdapter {

    fun Optional<Predicate>.mapToPredicateRepresentation(): Optional<PredicateRepresentation> =
        map { it.toPredicateRepresentation() }

    fun Page<Predicate>.mapToPredicateRepresentation(): Page<PredicateRepresentation> =
        map { it.toPredicateRepresentation() }

    fun Predicate.toPredicateRepresentation(): PredicateRepresentation =
        object : PredicateRepresentation {
            override val id: ThingId = this@toPredicateRepresentation.id
            override val label: String = this@toPredicateRepresentation.label
            override val description: String? = this@toPredicateRepresentation.description
            override val jsonClass: String = "predicate"
            override val createdAt: OffsetDateTime = this@toPredicateRepresentation.createdAt
            override val createdBy: ContributorId = this@toPredicateRepresentation.createdBy
        }
}
