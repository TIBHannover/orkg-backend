package eu.tib.orkg.prototype.statements.application.port.`in`

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.Optional

interface MarkAsVerifiedUseCase {
    /**
     * Marks a resource as verified.
     *
     * @param resourceId The ID of the resource to modify.
     * @return The updated resource if successfully modified, or an empty [Optional] otherwise.
     */
    fun markAsVerified(resourceId: ResourceId)

    /**
     * Marks a resource as unverified.
     *
     * @param resourceId The ID of the resource to modify.
     * @return The updated resource if successfully modified, or an empty [Optional] otherwise.
     */
    fun markAsUnverified(resourceId: ResourceId)
}

interface MarkFeaturedService {
    fun markAsFeatured(resourceId: ResourceId)
    fun markAsNonFeatured(resourceId: ResourceId)
}

interface MarkAsUnlistedService {
    fun markAsUnlisted(resourceId: ResourceId)
    fun markAsListed(resourceId: ResourceId)
}
