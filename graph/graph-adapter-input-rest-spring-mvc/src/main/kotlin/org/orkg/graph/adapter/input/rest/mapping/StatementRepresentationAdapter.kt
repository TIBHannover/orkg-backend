package org.orkg.graph.adapter.input.rest.mapping

import java.util.*
import org.orkg.common.MediaTypeCapabilities
import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementCounts
import org.orkg.graph.adapter.input.rest.StatementRepresentation
import org.springframework.data.domain.Page

interface StatementRepresentationAdapter : ThingRepresentationAdapter {

    fun Optional<GeneralStatement>.mapToStatementRepresentation(
        capabilities: MediaTypeCapabilities
    ): Optional<StatementRepresentation> =
        map { it.toStatementRepresentation(capabilities) }

    fun Page<GeneralStatement>.mapToStatementRepresentation(
        capabilities: MediaTypeCapabilities
    ): Page<StatementRepresentation> {
        val resources = content.resources()
        val statementCounts = countIncomingStatements(resources)
        val formattedLabelCounts = formatLabelFor(resources, capabilities)
        return map { it.toRepresentation(statementCounts, formattedLabelCounts) }
    }

    fun Iterable<GeneralStatement>.mapToStatementRepresentation(
        capabilities: MediaTypeCapabilities
    ): Iterable<StatementRepresentation> {
        val resources = toList().resources()
        val statementCounts = countIncomingStatements(resources)
        val formattedLabelCounts = formatLabelFor(resources, capabilities)
        return map { it.toRepresentation(statementCounts, formattedLabelCounts) }
    }

    private fun GeneralStatement.toStatementRepresentation(
        capabilities: MediaTypeCapabilities
    ): StatementRepresentation {
        val resources = listOf(this).resources()
        val counts = countIncomingStatements(resources)
        val labels = formatLabelFor(resources, capabilities)
        return toRepresentation(counts, labels)
    }

    private fun GeneralStatement.toRepresentation(
        statementCounts: StatementCounts,
        formattedLabels: FormattedLabels
    ): StatementRepresentation =
        StatementRepresentation(
            id = id,
            subject = subject.toThingRepresentation(statementCounts, formattedLabels),
            predicate = predicate.toPredicateRepresentation(),
            `object` = `object`.toThingRepresentation(statementCounts, formattedLabels),
            createdAt = createdAt!!,
            createdBy = createdBy,
            modifiable = modifiable,
            index = index
        )

    private fun List<GeneralStatement>.resources() =
        map(GeneralStatement::subject).filterIsInstance<Resource>() +
            map(GeneralStatement::`object`).filterIsInstance<Resource>()
}
