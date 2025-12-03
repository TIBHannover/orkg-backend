package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.community.domain.ContributorNotFound
import org.orkg.community.output.ContributorRepository
import org.orkg.graph.domain.NeitherOwnerNorCurator
import org.orkg.graph.domain.Resource

class ResourceOwnerValidator<T, S>(
    private val contributorRepository: ContributorRepository,
    private val resourceSelector: (S) -> Resource?,
    private val contributorIdSelector: (T) -> ContributorId,
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        val resource = resourceSelector(state)
        val contributorId = contributorIdSelector(command)
        if (resource != null && !resource.isOwnedBy(contributorId)) {
            val contributor = contributorRepository.findById(contributorId)
                .orElseThrow { ContributorNotFound(contributorId) }
            if (!contributor.isCurator) {
                throw NeitherOwnerNorCurator(resource.createdBy, contributorId, resource.id)
            }
        }
        return state
    }
}
