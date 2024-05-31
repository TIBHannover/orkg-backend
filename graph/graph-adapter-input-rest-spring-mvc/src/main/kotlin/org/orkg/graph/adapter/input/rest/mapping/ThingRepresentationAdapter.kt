package org.orkg.graph.adapter.input.rest.mapping

import java.util.*
import org.orkg.common.MediaTypeCapabilities
import org.orkg.graph.adapter.input.rest.ThingRepresentation
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementCounts
import org.orkg.graph.domain.Thing
import org.springframework.data.domain.Page

interface ThingRepresentationAdapter : ResourceRepresentationAdapter, ClassRepresentationAdapter,
    LiteralRepresentationAdapter, PredicateRepresentationAdapter, ListRepresentationAdapter {

    fun Optional<Thing>.mapToThingRepresentation(
        capabilities: MediaTypeCapabilities
    ): Optional<ThingRepresentation> =
        map { it.toThingRepresentation(capabilities) }

    fun Page<Thing>.mapToThingRepresentation(
        capabilities: MediaTypeCapabilities
    ): Page<ThingRepresentation> {
        val resources = content.filterIsInstance<Resource>()
        val statementCounts = countIncomingStatements(resources)
        val formattedLabelCount = formatLabelFor(resources, capabilities)
        return map { it.toThingRepresentation(statementCounts, formattedLabelCount) }
    }

    private fun Thing.toThingRepresentation(
        capabilities: MediaTypeCapabilities
    ): ThingRepresentation =
        when (this) {
            is Resource -> {
                val count = statementService.countIncomingStatements(id)
                toResourceRepresentation(mapOf(id to count), formatLabelFor(listOf(this), capabilities))
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
