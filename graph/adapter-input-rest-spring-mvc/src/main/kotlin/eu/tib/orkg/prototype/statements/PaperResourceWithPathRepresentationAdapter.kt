package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.PaperResourceWithPathRepresentation
import eu.tib.orkg.prototype.statements.api.PathRepresentation
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.FormattedLabel
import eu.tib.orkg.prototype.statements.domain.model.PaperResourceWithPath
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.statements.services.FormattedLabels
import eu.tib.orkg.prototype.statements.services.StatementCounts
import java.time.OffsetDateTime
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
        object : PaperResourceWithPathRepresentation {
            override val id: ThingId = this@toPaperResourceWithPathRepresentation.paper.id
            override val label: String = this@toPaperResourceWithPathRepresentation.paper.label
            override val classes: Set<ThingId> = this@toPaperResourceWithPathRepresentation.paper.classes
            override val shared: Long = usageCounts[this@toPaperResourceWithPathRepresentation.paper.id] ?: 0
            override val extractionMethod: ExtractionMethod = this@toPaperResourceWithPathRepresentation.paper.extractionMethod
            override val jsonClass: String = "resource"
            override val createdAt: OffsetDateTime = this@toPaperResourceWithPathRepresentation.paper.createdAt
            override val createdBy: ContributorId = this@toPaperResourceWithPathRepresentation.paper.createdBy
            override val observatoryId: ObservatoryId = this@toPaperResourceWithPathRepresentation.paper.observatoryId
            override val organizationId: OrganizationId = this@toPaperResourceWithPathRepresentation.paper.organizationId
            override val featured: Boolean = this@toPaperResourceWithPathRepresentation.paper.visibility == Visibility.FEATURED
            override val unlisted: Boolean = this@toPaperResourceWithPathRepresentation.paper.visibility == Visibility.UNLISTED
            override val verified: Boolean = this@toPaperResourceWithPathRepresentation.paper.verified ?: false
            override val unlistedBy: ContributorId? = this@toPaperResourceWithPathRepresentation.paper.unlistedBy
            override val formattedLabel: FormattedLabel? = formattedLabels[this@toPaperResourceWithPathRepresentation.paper.id]
            override val path: PathRepresentation = this@toPaperResourceWithPathRepresentation.path.map { list ->
                list.map { it.toThingRepresentation(usageCounts, formattedLabels) }
            }
        }
}
