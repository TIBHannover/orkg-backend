package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.TemplateInstanceRepresentation
import org.orkg.contenttypes.domain.TemplateInstance
import org.orkg.graph.adapter.input.rest.mapping.ThingRepresentationAdapter
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementCounts
import org.orkg.graph.domain.Thing
import org.springframework.data.domain.Page
import java.util.Optional

interface TemplateInstanceRepresentationAdapter :
    ThingRepresentationAdapter,
    EmbeddedStatementRepresentationAdapter {
    fun Optional<TemplateInstance>.mapToTemplateInstanceRepresentation(
        capabilities: MediaTypeCapabilities,
    ): Optional<TemplateInstanceRepresentation> =
        map { it.toTemplateInstanceRepresentation(capabilities) }

    fun Page<TemplateInstance>.mapToTemplateInstanceRepresentation(
        capabilities: MediaTypeCapabilities,
    ): Page<TemplateInstanceRepresentation> {
        val resources = content.flatMap { it.resources() }
        val statementCounts = countIncomingStatements(resources)
        val formattedLabels = formatLabelFor(resources, capabilities)
        val descriptions = findAllDescriptions(content.flatMap { it.thingsWithDescription() })
        return map { it.toTemplateInstanceRepresentation(statementCounts, formattedLabels, descriptions) }
    }

    private fun TemplateInstance.toTemplateInstanceRepresentation(
        capabilities: MediaTypeCapabilities,
    ): TemplateInstanceRepresentation {
        val resources = resources()
        val counts = countIncomingStatements(resources)
        val labels = formatLabelFor(resources, capabilities)
        val descriptions = findAllDescriptions(thingsWithDescription())
        return toTemplateInstanceRepresentation(counts, labels, descriptions)
    }

    private fun TemplateInstance.toTemplateInstanceRepresentation(
        statementCounts: StatementCounts,
        formattedLabels: FormattedLabels,
        descriptions: Map<ThingId, String>,
    ): TemplateInstanceRepresentation =
        TemplateInstanceRepresentation(
            root = root.toResourceRepresentation(statementCounts, formattedLabels),
            statements = statements.mapValues { (_, value) ->
                value.map { it.toEmbeddedStatementRepresentation(statementCounts, formattedLabels, descriptions) }
            }
        )

    fun TemplateInstance.resources(): List<Resource> =
        (statements.values.flatMap { it.resources() } + root)

    fun TemplateInstance.thingsWithDescription(): List<Thing> =
        statements.values.flatMap { it.thingsWithDescription() }.filter { it is Predicate || it is Class }
}
