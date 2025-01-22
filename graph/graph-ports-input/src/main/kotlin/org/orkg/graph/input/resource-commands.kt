package org.orkg.graph.input

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Visibility

interface CreateResourceUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val id: ThingId? = null,
        val label: String,
        val classes: Set<ThingId> = emptySet(),
        val extractionMethod: ExtractionMethod? = null,
        val contributorId: ContributorId? = null,
        val observatoryId: ObservatoryId? = null,
        val organizationId: OrganizationId? = null,
        val modifiable: Boolean = true
    )
}

interface UpdateResourceUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val id: ThingId,
        val contributorId: ContributorId,
        val label: String? = null,
        val classes: Set<ThingId>? = null,
        val observatoryId: ObservatoryId? = null,
        val organizationId: OrganizationId? = null,
        val extractionMethod: ExtractionMethod? = null,
        val modifiable: Boolean? = null,
        val visibility: Visibility? = null,
        val verified: Boolean? = null,
    )
}

interface DeleteResourceUseCase {
    // legacy methods:
    fun delete(id: ThingId, contributorId: ContributorId)
    fun removeAll()
}

interface MarkAsVerifiedUseCase {
    /**
     * Marks a resource as verified.
     *
     * @param resourceId The ID of the resource to modify.
     * @return The updated resource if successfully modified, or an empty [Optional] otherwise.
     */
    fun markAsVerified(resourceId: ThingId)

    /**
     * Marks a resource as unverified.
     *
     * @param resourceId The ID of the resource to modify.
     * @return The updated resource if successfully modified, or an empty [Optional] otherwise.
     */
    fun markAsUnverified(resourceId: ThingId)
}

interface MarkFeaturedService {
    fun markAsFeatured(resourceId: ThingId)
    fun markAsNonFeatured(resourceId: ThingId)
}

interface MarkAsUnlistedService {
    fun markAsUnlisted(resourceId: ThingId, contributorId: ContributorId)
    fun markAsListed(resourceId: ThingId)
}
