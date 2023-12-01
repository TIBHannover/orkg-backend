package org.orkg.contenttypes.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Visibility

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