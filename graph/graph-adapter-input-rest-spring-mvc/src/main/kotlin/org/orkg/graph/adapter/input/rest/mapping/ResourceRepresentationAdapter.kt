package org.orkg.graph.adapter.input.rest.mapping

import java.util.*
import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementCounts
import org.orkg.graph.domain.Visibility
import org.orkg.graph.input.ResourceRepresentation
import org.springframework.data.domain.Page

interface ResourceRepresentationAdapter : FormattedLabelRepresentationAdapter {
    fun Optional<Resource>.mapToResourceRepresentation(): Optional<ResourceRepresentation> =
        map {
            val count = statementService.countStatementsAboutResource(it.id)
            it.toResourceRepresentation(mapOf(it.id to count), formatLabelFor(listOf(it)))
        }

    fun Page<Resource>.mapToResourceRepresentation(): Page<ResourceRepresentation> {
        val statementCounts = countsFor(content)
        val formattedLabelCount = formatLabelFor(content)
        return map { it.toResourceRepresentation(statementCounts, formattedLabelCount) }
    }

    fun Iterable<Resource>.mapToResourceRepresentation(): Iterable<ResourceRepresentation> {
        val statementCounts = countsFor(toList())
        val formattedLabelCount = formatLabelFor(toList())
        return map { it.toResourceRepresentation(statementCounts, formattedLabelCount) }
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
            verified = verified ?: false,
            unlistedBy = unlistedBy,
            modifiable = modifiable,
            formattedLabel = formattedLabels[this@toResourceRepresentation.id],
            extractionMethod = extractionMethod
        )
}
