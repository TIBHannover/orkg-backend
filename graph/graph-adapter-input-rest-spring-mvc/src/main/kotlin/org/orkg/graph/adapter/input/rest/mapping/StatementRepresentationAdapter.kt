package org.orkg.graph.adapter.input.rest.mapping

import java.util.*
import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementCounts
import org.orkg.graph.adapter.input.rest.StatementRepresentation
import org.springframework.data.domain.Page

interface StatementRepresentationAdapter : ThingRepresentationAdapter {

    fun Optional<GeneralStatement>.mapToStatementRepresentation(): Optional<StatementRepresentation> =
        map { it.toStatementRepresentation() }

    fun Page<GeneralStatement>.mapToStatementRepresentation(): Page<StatementRepresentation> {
        val resources = content.resources()
        val statementCounts = countsFor(resources)
        val formattedLabelCounts = formatLabelFor(resources)
        return map { it.toRepresentation(statementCounts, formattedLabelCounts) }
    }

    fun Iterable<GeneralStatement>.mapToStatementRepresentation(): Iterable<StatementRepresentation> {
        val resources = toList().resources()
        val statementCounts = countsFor(resources)
        val formattedLabelCounts = formatLabelFor(resources)
        return map { it.toRepresentation(statementCounts, formattedLabelCounts) }
    }

    private fun GeneralStatement.toStatementRepresentation(): StatementRepresentation {
        val resources = listOf(this).resources()
        val counts = countsFor(resources)
        val labels = formatLabelFor(resources)
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
