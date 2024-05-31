package org.orkg.contenttypes.adapter.input.rest.mapping

import java.util.*
import org.orkg.common.MediaTypeCapabilities
import org.orkg.contenttypes.adapter.input.rest.EmbeddedStatementRepresentation
import org.orkg.contenttypes.domain.EmbeddedStatement
import org.orkg.graph.adapter.input.rest.mapping.ThingRepresentationAdapter
import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementCounts
import org.springframework.data.domain.Page

interface EmbeddedStatementRepresentationAdapter : ThingRepresentationAdapter {

    fun Optional<EmbeddedStatement>.mapToEmbeddedStatementRepresentation(
        capabilities: MediaTypeCapabilities
    ): Optional<EmbeddedStatementRepresentation> =
        map { it.toEmbeddedStatementRepresentation(capabilities) }

    fun Page<EmbeddedStatement>.mapToEmbeddedStatementRepresentation(
        capabilities: MediaTypeCapabilities
    ): Page<EmbeddedStatementRepresentation> {
        val resources = content.resources()
        val statementCounts = countIncomingStatements(resources)
        val formattedLabels = formatLabelFor(resources, capabilities)
        return map { it.toEmbeddedStatementRepresentation(statementCounts, formattedLabels) }
    }

    private fun EmbeddedStatement.toEmbeddedStatementRepresentation(
        capabilities: MediaTypeCapabilities
    ): EmbeddedStatementRepresentation {
        val resources = listOf(this).resources()
        val counts = countIncomingStatements(resources)
        val labels = formatLabelFor(resources, capabilities)
        return toEmbeddedStatementRepresentation(counts, labels)
    }

    fun EmbeddedStatement.toEmbeddedStatementRepresentation(
        statementCounts: StatementCounts,
        formattedLabels: FormattedLabels
    ): EmbeddedStatementRepresentation =
        EmbeddedStatementRepresentation(
            thing = thing.toThingRepresentation(statementCounts, formattedLabels),
            createdAt = createdAt,
            createdBy = createdBy,
            statements = statements.mapValues { (_, value) -> value.map { it.toEmbeddedStatementRepresentation(statementCounts, formattedLabels) } }
        )

    private fun List<EmbeddedStatement>.resources(): List<Resource> =
        flatMap { it.resources() }

    fun EmbeddedStatement.resources(): List<Resource> =
        (statements.values.flatMap { it.map { e -> e.resources() } } + thing).filterIsInstance<Resource>()
}
