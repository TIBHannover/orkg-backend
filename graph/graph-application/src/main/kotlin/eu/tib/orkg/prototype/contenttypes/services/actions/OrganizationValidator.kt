package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.community.application.OrganizationNotFound
import eu.tib.orkg.prototype.contenttypes.api.CreatePaperUseCase
import eu.tib.orkg.prototype.contenttypes.application.OnlyOneOrganizationAllowed

class OrganizationValidator(
    private val organizationRepository: PostgresOrganizationRepository
) : PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: PaperState): PaperState {
        if (command.organizations.size > 1) throw OnlyOneOrganizationAllowed()
        command.organizations.distinct().forEach {
            organizationRepository.findById(it.value).orElseThrow { OrganizationNotFound(it) }
        }
        return state
    }
}
