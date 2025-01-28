package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.community.domain.ContributorNotFound
import org.orkg.community.output.ContributorRepository
import org.orkg.graph.domain.NotACurator

class VerifiedValidator<T, S>(
    private val contributorRepository: ContributorRepository,
    private val contributorSelector: (T) -> ContributorId,
    private val oldValueSelector: (S) -> Boolean,
    private val newValueSelector: (T) -> Boolean?
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        val newVisibility = newValueSelector(command)
        val oldVisibility = oldValueSelector(state)
        if (newVisibility != null && newVisibility != oldVisibility) {
            val contributorId = contributorSelector(command)
            val contributor = contributorRepository.findById(contributorId)
                .orElseThrow { ContributorNotFound(contributorId) }
            if (!contributor.isCurator) {
                throw NotACurator.cannotChangeVerifiedStatus(contributorId)
            }
        }
        return state
    }
}
