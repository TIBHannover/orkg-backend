package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contenttypes.domain.model.Visibility
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.FormattedLabel
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.services.FormattedLabels
import eu.tib.orkg.prototype.statements.services.StatementCounts
import java.time.OffsetDateTime
import java.util.*
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
        object : ResourceRepresentation {
            override val id: ThingId = this@toResourceRepresentation.id
            override val label: String = this@toResourceRepresentation.label
            override val classes: Set<ThingId> = this@toResourceRepresentation.classes
            override val shared: Long = usageCounts[this@toResourceRepresentation.id] ?: 0
            override val extractionMethod: ExtractionMethod = this@toResourceRepresentation.extractionMethod
            override val jsonClass: String = "resource"
            override val createdAt: OffsetDateTime = this@toResourceRepresentation.createdAt
            override val createdBy: ContributorId = this@toResourceRepresentation.createdBy
            override val observatoryId: ObservatoryId = this@toResourceRepresentation.observatoryId
            override val organizationId: OrganizationId = this@toResourceRepresentation.organizationId
            override val featured: Boolean = this@toResourceRepresentation.visibility == Visibility.FEATURED
            override val unlisted: Boolean = this@toResourceRepresentation.visibility == Visibility.UNLISTED
            override val verified: Boolean = this@toResourceRepresentation.verified ?: false
            override val unlistedBy: ContributorId? = this@toResourceRepresentation.unlistedBy
            override val formattedLabel: FormattedLabel? = formattedLabels[this@toResourceRepresentation.id]
        }
}
