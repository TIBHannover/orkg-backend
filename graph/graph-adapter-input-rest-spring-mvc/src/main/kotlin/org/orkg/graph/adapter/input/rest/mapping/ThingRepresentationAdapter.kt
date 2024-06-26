package org.orkg.graph.adapter.input.rest.mapping

import java.util.*
import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
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
        val descriptions = findAllDescriptions(content.filterIsInstance<Predicate>())
        return map { it.toThingRepresentation(statementCounts, formattedLabelCount, descriptions[it.id]) }
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
            is Predicate -> {
                val description = statementService.findAllDescriptions(setOf(id))
                toPredicateRepresentation(description[id])
            }
        }

    fun Thing.toThingRepresentation(
        statementCounts: StatementCounts,
        formattedLabels: FormattedLabels,
        description: String?
    ): ThingRepresentation =
        when (this) {
            is Resource -> toResourceRepresentation(statementCounts, formattedLabels)
            is Class -> toClassRepresentation()
            is Literal -> toLiteralRepresentation()
            is Predicate -> toPredicateRepresentation(description)
        }

    fun findAllDescriptions(ids: List<Thing>): Map<ThingId, String> {
        if (ids.isEmpty()) {
            return emptyMap()
        }
        val predicateIds = ids.mapTo(mutableSetOf()) { it.id }
        return statementService.findAllDescriptions(predicateIds)
    }
}
