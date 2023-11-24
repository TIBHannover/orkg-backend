package org.orkg.contenttypes.domain.actions

import org.orkg.common.OrganizationId
import org.orkg.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.contenttypes.domain.OnlyOneOrganizationAllowed

abstract class OrganizationValidator(
    private val organizationRepository: PostgresOrganizationRepository
) {
    internal fun validate(organizations: List<OrganizationId>) {
        if (organizations.size > 1) throw OnlyOneOrganizationAllowed()
        organizations.distinct().forEach {
            organizationRepository.findById(it.value).orElseThrow { OrganizationNotFound(it) }
        }
    }
}
