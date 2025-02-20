package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.EmbeddedStatementRepresentation
import org.orkg.contenttypes.domain.EmbeddedStatement
import org.orkg.graph.adapter.input.rest.mapping.ThingRepresentationAdapter
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementCounts
import org.orkg.graph.domain.Thing
import org.springframework.data.domain.Page
import java.util.Optional

interface EmbeddedStatementRepresentationAdapter : ThingRepresentationAdapter {
    fun Optional<EmbeddedStatement>.mapToEmbeddedStatementRepresentation(
        capabilities: MediaTypeCapabilities,
    ): Optional<EmbeddedStatementRepresentation> =
        map { it.toEmbeddedStatementRepresentation(capabilities) }

    fun Page<EmbeddedStatement>.mapToEmbeddedStatementRepresentation(
        capabilities: MediaTypeCapabilities,
    ): Page<EmbeddedStatementRepresentation> {
        val resources = content.resources()
        val statementCounts = countIncomingStatements(resources)
        val formattedLabels = formatLabelFor(resources, capabilities)
        val descriptions = findAllDescriptions(content.thingsWithDescription())
        return map { it.toEmbeddedStatementRepresentation(statementCounts, formattedLabels, descriptions) }
    }

    private fun EmbeddedStatement.toEmbeddedStatementRepresentation(
        capabilities: MediaTypeCapabilities,
    ): EmbeddedStatementRepresentation {
        val resources = resources()
        val counts = countIncomingStatements(resources)
        val labels = formatLabelFor(resources, capabilities)
        val descriptions = findAllDescriptions(thingsWithDescription())
        return toEmbeddedStatementRepresentation(counts, labels, descriptions)
    }

    fun EmbeddedStatement.toEmbeddedStatementRepresentation(
        statementCounts: StatementCounts,
        formattedLabels: FormattedLabels,
        descriptions: Map<ThingId, String>,
    ): EmbeddedStatementRepresentation =
        EmbeddedStatementRepresentation(
            thing = thing.toThingRepresentation(statementCounts, formattedLabels, descriptions[thing.id]),
            createdAt = createdAt,
            createdBy = createdBy,
            statements = statements.mapValues { (_, value) ->
                value.map { it.toEmbeddedStatementRepresentation(statementCounts, formattedLabels, descriptions) }
            }
        )

    fun List<EmbeddedStatement>.resources(): List<Resource> =
        flatMap { it.resources() }

    fun EmbeddedStatement.resources(): List<Resource> =
        (statements.values.flatMap { it.resources() } + thing).filterIsInstance<Resource>()

    fun List<EmbeddedStatement>.thingsWithDescription(): List<Thing> =
        flatMap { it.thingsWithDescription() }

    fun EmbeddedStatement.thingsWithDescription(): List<Thing> =
        (statements.values.flatMap { it.thingsWithDescription() } + thing).filter { it is Predicate || it is Class }
}
