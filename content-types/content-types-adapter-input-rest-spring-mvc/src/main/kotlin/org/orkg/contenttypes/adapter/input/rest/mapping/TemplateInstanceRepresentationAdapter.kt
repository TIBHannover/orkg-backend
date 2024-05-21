package org.orkg.contenttypes.adapter.input.rest.mapping

import java.util.*
import org.orkg.contenttypes.adapter.input.rest.TemplateInstanceRepresentation
import org.orkg.contenttypes.domain.TemplateInstance
import org.orkg.graph.adapter.input.rest.mapping.ThingRepresentationAdapter
import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementCounts
import org.springframework.data.domain.Page

interface TemplateInstanceRepresentationAdapter : ThingRepresentationAdapter, EmbeddedStatementRepresentationAdapter {

    fun Optional<TemplateInstance>.mapToTemplateInstanceRepresentation(): Optional<TemplateInstanceRepresentation> =
        map { it.toTemplateInstanceRepresentation() }

    fun Page<TemplateInstance>.mapToTemplateInstanceRepresentation(): Page<TemplateInstanceRepresentation> {
        val resources = content.resources()
        val statementCounts = countIncomingStatements(resources)
        val formattedLabels = formatLabelFor(resources)
        return map { it.toTemplateInstanceRepresentation(statementCounts, formattedLabels) }
    }

    private fun TemplateInstance.toTemplateInstanceRepresentation(): TemplateInstanceRepresentation {
        val resources = listOf(this).resources()
        val counts = countIncomingStatements(resources)
        val labels = formatLabelFor(resources)
        return toTemplateInstanceRepresentation(counts, labels)
    }

    private fun TemplateInstance.toTemplateInstanceRepresentation(
        statementCounts: StatementCounts,
        formattedLabels: FormattedLabels
    ): TemplateInstanceRepresentation =
        TemplateInstanceRepresentation(
            root = root.toResourceRepresentation(statementCounts, formattedLabels),
            statements = statements.mapValues { (_, value) -> value.map { it.toEmbeddedStatementRepresentation(statementCounts, formattedLabels) } }
        )

    private fun List<TemplateInstance>.resources(): List<Resource> =
        flatMap { it.resources() }

    fun TemplateInstance.resources(): List<Resource> =
        (statements.values.flatMap { it.map { e -> e.resources() } } + root).filterIsInstance<Resource>()
}
