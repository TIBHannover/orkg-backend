package eu.tib.orkg.prototype.contenttypes.domain

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.time.OffsetDateTime

typealias ContentTypeId = String

sealed interface ContentType {
    val id: ContentTypeId
}

class Contribution(override val id: ContentTypeId) : ContentType

class Paper(
    override val id: ContentTypeId,
    val title: String,
    val researchField: ResourceId,
    val identifiers: PaperIdentifiers,
    val publicationInfo: PublicationInfo,
    val authors: List<ThingId>, // TODO ThingId? and/or Set?
    val contributors: List<ContributorId>,
    val contributions: List<ResourceId>,
    val observatories: List<ObservatoryId>,
    val organizations: List<OrganizationId>,
    val extractionMethod: ExtractionMethod,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId,
    val featured: Boolean,
    val unlisted: Boolean,
    val verified: Boolean,
    val deleted: Boolean
) : ContentType

class ResearchProblem(override val id: ContentTypeId) : ContentType

class Visualization(override val id: ContentTypeId) : ContentType

data class PublicationInfo(
    val publishedMonth: Int,
    val publishedYear: Long,
    val publishedIn: String?,
    val downloadUrl: String?
)

data class PaperIdentifiers(
    val doi: String?
)
