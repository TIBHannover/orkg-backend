package eu.tib.orkg.prototype.statements.application.port.`in`

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ContributionInfo
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

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

interface RetrieveComparisonContributionsUseCase {
    fun findContributionsDetailsById(ids: List<ThingId>, pageable: Pageable): Page<ContributionInfo>
}
