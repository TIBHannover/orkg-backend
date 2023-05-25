package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.statements.api.ThingRepresentation
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.services.FormattedLabels
import eu.tib.orkg.prototype.statements.services.StatementCounts
import java.util.*

interface ThingRepresentationAdapter : ResourceRepresentationAdapter, ClassRepresentationAdapter,
    LiteralRepresentationAdapter, PredicateRepresentationAdapter {

    fun Optional<Thing>.mapToThingRepresentation(): Optional<ThingRepresentation> =
        map { it.toThingRepresentation() }

    private fun Thing.toThingRepresentation(): ThingRepresentation =
        when (this) {
            is Resource -> {
                val count = statementRepository.countStatementsAboutResource(id)
                toResourceRepresentation(mapOf(id to count), formatLabelFor(listOf(this)))
            }
            is Class -> toClassRepresentation()
            is Literal -> toLiteralRepresentation()
            is Predicate -> toPredicateRepresentation()
        }

    fun Thing.toThingRepresentation(
        statementCounts: StatementCounts,
        formattedLabels: FormattedLabels
    ): ThingRepresentation =
        when (this) {
            is Resource -> toResourceRepresentation(statementCounts, formattedLabels)
            is Class -> toClassRepresentation()
            is Literal -> toLiteralRepresentation()
            is Predicate -> toPredicateRepresentation()
        }
}
