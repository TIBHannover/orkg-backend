package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.statements.api.PaperResourceWithPathRepresentation
import eu.tib.orkg.prototype.statements.domain.model.PaperResourceWithPath
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.statements.services.FormattedLabels
import eu.tib.orkg.prototype.statements.services.StatementCounts
import org.springframework.data.domain.Page

interface PaperResourceWithPathRepresentationAdapter : ThingRepresentationAdapter {

    fun Page<PaperResourceWithPath>.mapToPaperResourceWithPathRepresentation(): Page<PaperResourceWithPathRepresentation> {
        val resources = map { it.paper } + map { it.path }.flatten().filterIsInstance<Resource>()
        val usageCounts = countsFor(resources)
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
