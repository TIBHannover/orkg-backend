package org.orkg.graph.adapter.input.rest.mapping

import java.util.*
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementCounts
import org.orkg.graph.domain.Thing
import org.orkg.graph.adapter.input.rest.ThingRepresentation
import org.springframework.data.domain.Page

interface ThingRepresentationAdapter : ResourceRepresentationAdapter, ClassRepresentationAdapter,
    LiteralRepresentationAdapter, PredicateRepresentationAdapter, ListRepresentationAdapter {

    fun Optional<Thing>.mapToThingRepresentation(): Optional<ThingRepresentation> =
        map { it.toThingRepresentation() }

    fun Page<Thing>.mapToThingRepresentation(): Page<ThingRepresentation> {
        val resources = content.filterIsInstance<Resource>()
        val statementCounts = countIncomingStatements(resources)
        val formattedLabelCount = formatLabelFor(resources)
        return map { it.toThingRepresentation(statementCounts, formattedLabelCount) }
    }

    private fun Thing.toThingRepresentation(): ThingRepresentation =
        when (this) {
            is Resource -> {
                val count = statementService.countIncomingStatements(id)
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
