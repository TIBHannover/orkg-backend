package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.community.application.OrganizationNotFound
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contenttypes.application.OnlyOneOrganizationAllowed

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
