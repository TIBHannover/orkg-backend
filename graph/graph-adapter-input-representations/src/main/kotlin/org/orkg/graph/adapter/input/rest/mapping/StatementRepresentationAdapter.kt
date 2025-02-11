package org.orkg.graph.adapter.input.rest.mapping

import java.util.*
import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.graph.adapter.input.rest.StatementRepresentation
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementCounts
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
        val descriptions = findAllDescriptions(thingsWithDescription())
        return map { it.toRepresentation(statementCounts, formattedLabelCounts, descriptions) }
    }

    fun Iterable<GeneralStatement>.mapToStatementRepresentation(
        capabilities: MediaTypeCapabilities
    ): Iterable<StatementRepresentation> {
        val resources = resources()
        val statementCounts = countIncomingStatements(resources)
        val formattedLabelCounts = formatLabelFor(resources, capabilities)
        val descriptions = findAllDescriptions(thingsWithDescription())
        return map { it.toRepresentation(statementCounts, formattedLabelCounts, descriptions) }
    }

    private fun GeneralStatement.toStatementRepresentation(
        capabilities: MediaTypeCapabilities
    ): StatementRepresentation {
        val statementAsList = listOf(this)
        val resources = statementAsList.resources()
        val counts = countIncomingStatements(resources)
        val labels = formatLabelFor(resources, capabilities)
        val descriptions = findAllDescriptions(statementAsList.thingsWithDescription())
        return toRepresentation(counts, labels, descriptions)
    }

    private fun GeneralStatement.toRepresentation(
        statementCounts: StatementCounts,
        formattedLabels: FormattedLabels,
        descriptions: Map<ThingId, String>
    ): StatementRepresentation =
        StatementRepresentation(
            id = id,
            subject = subject.toThingRepresentation(statementCounts, formattedLabels, descriptions[subject.id]),
            predicate = predicate.toPredicateRepresentation(descriptions[predicate.id]),
            `object` = `object`.toThingRepresentation(statementCounts, formattedLabels, descriptions[`object`.id]),
            createdAt = createdAt!!,
            createdBy = createdBy,
            modifiable = modifiable,
            index = index
        )

    private fun Iterable<GeneralStatement>.resources() =
        map(GeneralStatement::subject).filterIsInstance<Resource>() +
            map(GeneralStatement::`object`).filterIsInstance<Resource>()

    private fun Iterable<GeneralStatement>.thingsWithDescription() =
        map(GeneralStatement::subject).filter { it is Predicate || it is Class } +
            map(GeneralStatement::predicate) +
            map(GeneralStatement::`object`).filter { it is Predicate || it is Class }
}
