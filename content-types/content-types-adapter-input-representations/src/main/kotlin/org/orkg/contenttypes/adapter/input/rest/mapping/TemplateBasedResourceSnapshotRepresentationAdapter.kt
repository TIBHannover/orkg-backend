package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.TemplateBasedResourceSnapshotRepresentation
import org.orkg.contenttypes.domain.TemplateBasedResourceSnapshot
import org.orkg.contenttypes.domain.TemplateBasedResourceSnapshotV1
import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementCounts
import org.orkg.graph.domain.Thing
import org.springframework.data.domain.Page
import java.util.Optional

interface TemplateBasedResourceSnapshotRepresentationAdapter : TemplateInstanceRepresentationAdapter {
    fun Optional<TemplateBasedResourceSnapshot<*>>.mapToTemplateBasedResourceSnapshotRepresentation(
        capabilities: MediaTypeCapabilities,
    ): Optional<TemplateBasedResourceSnapshotRepresentation> =
        map { it.toTemplateBasedResourceSnapshotRepresentation(capabilities) }

    fun Page<TemplateBasedResourceSnapshot<*>>.mapToTemplateBasedResourceSnapshotRepresentation(
        capabilities: MediaTypeCapabilities,
    ): Page<TemplateBasedResourceSnapshotRepresentation> {
        val resources = content.flatMap { it.resources() }
        val statementCounts = countIncomingStatements(resources)
        val formattedLabels = formatLabelFor(resources, capabilities)
        val descriptions = findAllDescriptions(content.flatMap { it.thingsWithDescription() })
        return map { it.toTemplateBasedResourceSnapshotRepresentation(statementCounts, formattedLabels, descriptions) }
    }

    private fun TemplateBasedResourceSnapshot<*>.toTemplateBasedResourceSnapshotRepresentation(
        capabilities: MediaTypeCapabilities,
    ): TemplateBasedResourceSnapshotRepresentation {
        val resources = resources()
        val counts = countIncomingStatements(resources)
        val labels = formatLabelFor(resources, capabilities)
        val descriptions = findAllDescriptions(thingsWithDescription())
        return toTemplateBasedResourceSnapshotRepresentation(counts, labels, descriptions)
    }

    private fun TemplateBasedResourceSnapshot<*>.toTemplateBasedResourceSnapshotRepresentation(
        statementCounts: StatementCounts,
        formattedLabels: FormattedLabels,
        descriptions: Map<ThingId, String>,
    ): TemplateBasedResourceSnapshotRepresentation =
        when (this) {
            is TemplateBasedResourceSnapshotV1 -> TemplateBasedResourceSnapshotRepresentation(
                id = id,
                createdBy = createdBy,
                createdAt = createdAt,
                data = templateInstance.toTemplateInstanceRepresentation(statementCounts, formattedLabels, descriptions),
                resourceId = resourceId,
                templateId = templateId,
                handle = handle
            )
        }

    private fun TemplateBasedResourceSnapshot<*>.resources(): List<Resource> =
        when (this) {
            is TemplateBasedResourceSnapshotV1 -> data.resources()
        }

    fun TemplateBasedResourceSnapshot<*>.thingsWithDescription(): List<Thing> =
        when (this) {
            is TemplateBasedResourceSnapshotV1 -> data.thingsWithDescription()
        }
}
