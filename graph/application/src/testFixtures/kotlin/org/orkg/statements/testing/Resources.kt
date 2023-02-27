package org.orkg.statements.testing

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.time.OffsetDateTime

fun createResource(
    id: ThingId = ThingId("R1"),
    label: String = "Default Label",
    createdAt: OffsetDateTime = OffsetDateTime.now(),
    classes: Set<ThingId> = emptySet(),
    createdBy: ContributorId = ContributorId.createUnknownContributor(),
    observatoryId: ObservatoryId = ObservatoryId.createUnknownObservatory(),
    extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    organizationId: OrganizationId = OrganizationId.createUnknownOrganization(),
    featured: Boolean? = null,
    unlisted: Boolean? = null,
    verified: Boolean? = null
) = Resource(
    id = id,
    label = label,
    createdAt = createdAt,
    classes = classes,
    createdBy =createdBy,
    observatoryId = observatoryId,
    extractionMethod = extractionMethod,
    organizationId  =organizationId,
    featured = featured,
    unlisted = unlisted,
    verified = verified
)
