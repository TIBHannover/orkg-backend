package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.CreateResourceRequest
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import eu.tib.orkg.prototype.statements.application.UpdateResourceObservatoryRequest
import eu.tib.orkg.prototype.statements.application.UpdateResourceRequest
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId

interface CreateResourceUseCase {
    // legacy methods:
    fun create(label: String): ResourceRepresentation
    fun create(
        userId: ContributorId,
        label: String,
        observatoryId: ObservatoryId,
        extractionMethod: ExtractionMethod,
        organizationId: OrganizationId
    ): ResourceRepresentation

    fun create(request: CreateResourceRequest): ResourceRepresentation
    fun create(
        userId: ContributorId,
        request: CreateResourceRequest,
        observatoryId: ObservatoryId,
        extractionMethod: ExtractionMethod,
        organizationId: OrganizationId
    ): ResourceRepresentation
}

interface UpdateResourceUseCase {
    // legacy methods:
    fun update(request: UpdateResourceRequest): ResourceRepresentation
    fun updatePaperObservatory(request: UpdateResourceObservatoryRequest, id: ResourceId): ResourceRepresentation
}

interface DeleteResourceUseCase {
    // legacy methods:
    fun delete(id: ResourceId)
    fun removeAll()
}
