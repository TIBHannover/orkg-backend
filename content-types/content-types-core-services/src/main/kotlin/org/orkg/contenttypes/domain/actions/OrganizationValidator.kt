package org.orkg.contenttypes.domain.actions

import org.orkg.common.OrganizationId
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.domain.OnlyOneOrganizationAllowed

class OrganizationValidator<T, S>(
    private val organizationRepository: OrganizationRepository,
    private val valueSelector: (T) -> List<OrganizationId>?
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        val organizations = valueSelector(command)
        if (organizations != null) {
            if (organizations.size > 1) {
                throw OnlyOneOrganizationAllowed()
            }
            organizations.distinct().forEach {
                organizationRepository.findById(it).orElseThrow { OrganizationNotFound(it) }
            }
        }
        return state
    }
}
