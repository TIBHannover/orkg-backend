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
import org.springframework.data.domain.Page

interface ThingRepresentationAdapter : ResourceRepresentationAdapter, ClassRepresentationAdapter,
    LiteralRepresentationAdapter, PredicateRepresentationAdapter, ListRepresentationAdapter {

    fun Optional<Thing>.mapToThingRepresentation(): Optional<ThingRepresentation> =
        map { it.toThingRepresentation() }

    fun Page<Thing>.mapToThingRepresentation(): Page<ThingRepresentation> {
        val resources = content.filterIsInstance<Resource>()
        val statementCounts = countsFor(resources)
        val formattedLabelCount = formatLabelFor(resources)
        return map { it.toThingRepresentation(statementCounts, formattedLabelCount) }
    }

    private fun Thing.toThingRepresentation(): ThingRepresentation =
        when (this) {
            is Resource -> {
                val count = statementService.countStatementsAboutResource(id)
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
