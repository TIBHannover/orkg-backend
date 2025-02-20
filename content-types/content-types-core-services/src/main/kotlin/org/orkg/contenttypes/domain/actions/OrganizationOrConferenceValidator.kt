package org.orkg.contenttypes.domain.actions

import org.orkg.common.OrganizationId
import org.orkg.community.domain.ConferenceSeriesId
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.community.output.ConferenceSeriesRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.domain.OnlyOneOrganizationAllowed

@Deprecated("Conference series should be refactored to have their own field")
class OrganizationOrConferenceValidator<T, S>(
    private val organizationRepository: OrganizationRepository,
    private val conferenceSeriesRepository: ConferenceSeriesRepository,
    private val newValueSelector: (T) -> List<OrganizationId>?,
    private val oldValueSelector: (S) -> List<OrganizationId> = { emptyList() },
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        val newOrganizations = newValueSelector(command)
        val oldOrganizations = oldValueSelector(state)
        if (newOrganizations != null && newOrganizations.toSet() != oldOrganizations.toSet()) {
            if (newOrganizations.size > 1) {
                throw OnlyOneOrganizationAllowed()
            }
            (newOrganizations.distinct() - oldOrganizations.toSet()).forEach {
                if (organizationRepository.findById(it).isEmpty &&
                    conferenceSeriesRepository.findById(ConferenceSeriesId(it.value)).isEmpty
                ) {
                    throw OrganizationNotFound(it)
                }
            }
        }
        return state
    }
}
