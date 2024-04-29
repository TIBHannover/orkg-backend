package org.orkg.contenttypes.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Visibility

data class Paper(
    val id: ThingId,
    val title: String,
    val researchFields: List<ObjectIdAndLabel>,
    val identifiers: Map<String, List<String>>,
    val publicationInfo: PublicationInfo,
    val authors: List<Author>,
    val contributions: List<ObjectIdAndLabel>,
    val sustainableDevelopmentGoals: Set<ObjectIdAndLabel>,
    val observatories: List<ObservatoryId>,
    val organizations: List<OrganizationId>,
    val extractionMethod: ExtractionMethod,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId,
    val verified: Boolean,
    val visibility: Visibility,
    val modifiable: Boolean,
    val unlistedBy: ContributorId? = null
) : ContentType
