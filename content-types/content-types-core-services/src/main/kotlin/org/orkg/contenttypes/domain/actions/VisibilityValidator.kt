package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.community.domain.ContributorNotFound
import org.orkg.community.output.ContributorRepository
import org.orkg.contenttypes.domain.ContentType
import org.orkg.graph.domain.NeitherOwnerNorCurator
import org.orkg.graph.domain.Visibility

class VisibilityValidator<T, S>(
    private val contributorRepository: ContributorRepository,
    private val contributorSelector: (T) -> ContributorId,
    private val contentTypeExtractor: (S) -> ContentType,
    private val newValueSelector: (T) -> Visibility?
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        val contentType = contentTypeExtractor(state)
        val newVisibility = newValueSelector(command)
        val oldVisibility = contentType.visibility
        if (newVisibility != null && newVisibility != oldVisibility) {
            val contributorId = contributorSelector(command)
            if (!contentType.isOwnedBy(contributorId) || !isAllowedVisibilityChangeByOwner(oldVisibility, newVisibility)) {
                val contributor = contributorRepository.findById(contributorId)
                    .orElseThrow { ContributorNotFound(contributorId) }
                if (!contributor.isCurator) {
                    throw NeitherOwnerNorCurator.cannotChangeVisibility(contentType.id)
                }
            }
        }
        return state
    }

    private fun isAllowedVisibilityChangeByOwner(source: Visibility, target: Visibility) =
        source == Visibility.DELETED && target == Visibility.DEFAULT || // allow restoring deleted resources
            target == Visibility.DELETED // allow deletion of resources from any state
}
