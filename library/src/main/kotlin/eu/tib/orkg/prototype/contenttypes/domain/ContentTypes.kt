package eu.tib.orkg.prototype.contenttypes.domain

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.Resource
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
    val resource: Resource // TODO temporary (to be removed)
) : ContentType

class ResearchProblem(override val id: ContentTypeId) : ContentType

class Visualization(override val id: ContentTypeId) : ContentType

