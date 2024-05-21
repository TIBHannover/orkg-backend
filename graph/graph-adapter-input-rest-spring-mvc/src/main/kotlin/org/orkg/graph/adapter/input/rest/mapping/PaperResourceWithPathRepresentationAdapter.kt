package org.orkg.graph.adapter.input.rest.mapping

import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.PaperResourceWithPath
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementCounts
import org.orkg.graph.domain.Visibility
import org.orkg.graph.adapter.input.rest.PaperResourceWithPathRepresentation
import org.springframework.data.domain.Page

interface PaperResourceWithPathRepresentationAdapter : ThingRepresentationAdapter {

    fun Page<PaperResourceWithPath>.mapToPaperResourceWithPathRepresentation(): Page<PaperResourceWithPathRepresentation> {
        val resources = map { it.paper } + map { it.path }.flatten().filterIsInstance<Resource>()
        val usageCounts = countIncomingStatements(resources)
        val formattedLabels = formatLabelFor(resources)
        return map { it.toPaperResourceWithPathRepresentation(usageCounts, formattedLabels) }
    }

    fun PaperResourceWithPath.toPaperResourceWithPathRepresentation(
        usageCounts: StatementCounts,
        formattedLabels: FormattedLabels
    ): PaperResourceWithPathRepresentation =
        PaperResourceWithPathRepresentation(
            path = path.map { list -> list.map { it.toThingRepresentation(usageCounts, formattedLabels) } },
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
