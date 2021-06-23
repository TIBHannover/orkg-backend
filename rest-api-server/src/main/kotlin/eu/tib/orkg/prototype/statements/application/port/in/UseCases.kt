package eu.tib.orkg.prototype.statements.application.port.`in`

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.Optional

interface MarkAsVerifiedUseCase {
    /**
     * Marks a resource as verified.
     *
     * @param resourceId The ID of the resource to modify.
     * @return The updated resource if successfully modified, or an empty [Optional] otherwise.
     */
    fun markAsVerified(resourceId: ResourceId): Optional<Resource>

    /**
     * Marks a resource as unverified.
     *
     * @param resourceId The ID of the resource to modify.
     * @return The updated resource if successfully modified, or an empty [Optional] otherwise.
     */
    fun markAsUnverified(resourceId: ResourceId): Optional<Resource>
}

interface MarkFeaturedService {
    fun markAsFeatured(resourceId: ResourceId): Optional<Resource>
    fun markAsNonFeatured(resourceId: ResourceId): Optional<Resource>
}

interface MarkAsUnlistedService {
    fun markAsUnlisted(resourceId: ResourceId): Optional<Resource>
    fun markAsListed(resourceId: ResourceId): Optional<Resource>
}
