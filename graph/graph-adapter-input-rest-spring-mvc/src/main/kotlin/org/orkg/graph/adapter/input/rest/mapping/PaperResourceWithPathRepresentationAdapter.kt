package org.orkg.graph.adapter.input.rest.mapping

import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.graph.adapter.input.rest.PaperResourceWithPathRepresentation
import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.PaperResourceWithPath
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementCounts
import org.orkg.graph.domain.Visibility
import org.springframework.data.domain.Page

interface PaperResourceWithPathRepresentationAdapter : ThingRepresentationAdapter {

    fun Page<PaperResourceWithPath>.mapToPaperResourceWithPathRepresentation(
        capabilities: MediaTypeCapabilities
    ): Page<PaperResourceWithPathRepresentation> {
        val pathItems = map { it.path }.flatten()
        val resources = map { it.paper } + pathItems.filterIsInstance<Resource>()
        val usageCounts = countIncomingStatements(resources)
        val formattedLabels = formatLabelFor(resources, capabilities)
        val predicates = pathItems.filterIsInstance<Predicate>()
        val descriptions = findAllDescriptions(predicates)
        return map { it.toPaperResourceWithPathRepresentation(usageCounts, formattedLabels, descriptions) }
    }

    fun PaperResourceWithPath.toPaperResourceWithPathRepresentation(
        usageCounts: StatementCounts,
        formattedLabels: FormattedLabels,
        descriptions: Map<ThingId, String>
    ): PaperResourceWithPathRepresentation =
        PaperResourceWithPathRepresentation(
            path = path.map { list ->
                list.map { it.toThingRepresentation(usageCounts, formattedLabels, descriptions[it.id]) }
            },
            id = paper.id,
            label = paper.label,
            classes = paper.classes,
            shared = usageCounts[this@toPaperResourceWithPathRepresentation.paper.id] ?: 0,
            observatoryId = paper.observatoryId,
            organizationId = paper.organizationId,
            createdAt = paper.createdAt,
            createdBy = paper.createdBy,
            featured = paper.visibility == Visibility.FEATURED,
            unlisted = paper.visibility == Visibility.UNLISTED,
            verified = paper.verified ?: false,
            unlistedBy = paper.unlistedBy,
            formattedLabel = formattedLabels[this@toPaperResourceWithPathRepresentation.paper.id],
            extractionMethod = paper.extractionMethod
        )
}
