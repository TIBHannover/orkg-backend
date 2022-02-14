package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.CreateResourceRequest
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import eu.tib.orkg.prototype.statements.application.UpdateResourceObservatoryRequest
import eu.tib.orkg.prototype.statements.application.UpdateResourceRequest
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId

interface CreateResourceUseCase {
    // legacy methods:
    fun create(label: String): Resource
    fun create(
        userId: ContributorId,
        label: String,
        observatoryId: ObservatoryId,
        extractionMethod: ExtractionMethod,
        organizationId: OrganizationId
    ): Resource

    fun create(request: CreateResourceRequest): Resource
    fun create(
        userId: ContributorId,
        request: CreateResourceRequest,
        observatoryId: ObservatoryId,
        extractionMethod: ExtractionMethod,
        organizationId: OrganizationId
    ): Resource
}

interface UpdateResourceUseCase {
    // legacy methods:
    fun update(request: UpdateResourceRequest): Resource
    fun updatePaperObservatory(request: UpdateResourceObservatoryRequest, id: ResourceId): Resource
}

interface DeleteResourceUseCase {
    // legacy methods:
    fun delete(id: ResourceId)
    fun removeAll()
}
