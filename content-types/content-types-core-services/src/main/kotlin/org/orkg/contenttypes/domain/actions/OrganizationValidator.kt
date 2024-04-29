package org.orkg.contenttypes.domain.actions

import org.orkg.common.OrganizationId
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.domain.OnlyOneOrganizationAllowed

class OrganizationValidator<T, S>(
    private val organizationRepository: OrganizationRepository,
    private val newValueSelector: (T) -> List<OrganizationId>?,
    private val oldValueSelector: (S) -> List<OrganizationId> = { emptyList() }
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        val newOrganizations = newValueSelector(command)
        val oldOrganizations = oldValueSelector(state)
        if (newOrganizations != null && newOrganizations.toSet() != oldOrganizations.toSet()) {
            if (newOrganizations.size > 1) {
                throw OnlyOneOrganizationAllowed()
            }
            (newOrganizations.distinct() - oldOrganizations.toSet()).forEach {
                organizationRepository.findById(it).orElseThrow { OrganizationNotFound(it) }
            }
        }
        return state
    }
}
