package org.orkg.graph.adapter.input.rest.mapping

import java.util.*
import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementCounts
import org.orkg.graph.domain.Visibility
import org.orkg.graph.adapter.input.rest.ResourceRepresentation
import org.orkg.graph.input.StatementUseCases
import org.springframework.data.domain.Page

interface ResourceRepresentationAdapter : FormattedLabelRepresentationAdapter {
    val statementService: StatementUseCases

    fun Optional<Resource>.mapToResourceRepresentation(capabilities: MediaTypeCapabilities): Optional<ResourceRepresentation> =
        map {
            val count = statementService.countIncomingStatements(it.id)
            val formattedLabel = formatLabelFor(listOf(it), capabilities)
            it.toResourceRepresentation(mapOf(it.id to count), formattedLabel)
        }

    fun Page<Resource>.mapToResourceRepresentation(capabilities: MediaTypeCapabilities): Page<ResourceRepresentation> {
        val statementCounts = countIncomingStatements(content)
        val formattedLabelCount = formatLabelFor(content, capabilities)
        return map { it.toResourceRepresentation(statementCounts, formattedLabelCount) }
    }

    fun Iterable<Resource>.mapToResourceRepresentation(capabilities: MediaTypeCapabilities): Iterable<ResourceRepresentation> {
        val statementCounts = countIncomingStatements(toList())
        val formattedLabelCount = formatLabelFor(toList(), capabilities)
        return map { it.toResourceRepresentation(statementCounts, formattedLabelCount) }
    }

    fun countIncomingStatements(resources: List<Resource>): Map<ThingId, Long> {
        if (resources.isEmpty()) {
            return emptyMap()
        }
        val resourceIds = resources.map { it.id }.toSet()
        return statementService.countIncomingStatements(resourceIds)
    }

    fun Resource.toResourceRepresentation(
        usageCounts: StatementCounts,
        formattedLabels: FormattedLabels
    ): ResourceRepresentation =
        ResourceRepresentation(
            id = id,
            label = label,
            classes = classes,
            shared = usageCounts[this@toResourceRepresentation.id] ?: 0,
            observatoryId = observatoryId,
            organizationId = organizationId,
            createdAt = createdAt,
            createdBy = createdBy,
            featured = visibility == Visibility.FEATURED,
            unlisted = visibility == Visibility.UNLISTED,
            visibility = visibility,
            verified = verified ?: false,
            unlistedBy = unlistedBy,
            modifiable = modifiable,
            formattedLabel = formattedLabels[this@toResourceRepresentation.id],
            extractionMethod = extractionMethod
        )
}
