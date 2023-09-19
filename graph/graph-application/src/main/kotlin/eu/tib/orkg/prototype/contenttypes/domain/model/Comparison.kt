package eu.tib.orkg.prototype.contenttypes.domain.model

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import java.time.OffsetDateTime

data class Comparison(
    val id: ThingId,
    val title: String,
    val description: String?,
    val researchFields: List<ObjectIdAndLabel>,
    val identifiers: Map<String, String>,
    val publicationInfo: PublicationInfo,
    val authors: List<Author>,
    val contributions: List<ObjectIdAndLabel>,
    val visualizations: List<ObjectIdAndLabel>,
    val relatedFigures: List<ObjectIdAndLabel>,
    val relatedResources: List<ObjectIdAndLabel>,
    val references: List<String>,
    val observatories: List<ObservatoryId>,
    val organizations: List<OrganizationId>,
    val extractionMethod: ExtractionMethod,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId,
    val previousVersion: ThingId?,
    val isAnonymized: Boolean,
    val visibility: Visibility
)

data class ComparisonRelatedResource(
    val id: ThingId,
    val label: String,
    val image: String?,
    val url: String?,
    val description: String?
)

data class ComparisonRelatedFigure(
    val id: ThingId,
    val label: String,
    val image: String?,
    val description: String?
)
