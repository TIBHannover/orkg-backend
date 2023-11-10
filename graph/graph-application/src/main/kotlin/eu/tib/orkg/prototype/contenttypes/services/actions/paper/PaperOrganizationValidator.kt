package eu.tib.orkg.prototype.contenttypes.services.actions.paper

import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.contenttypes.services.actions.CreatePaperCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.OrganizationValidator
import eu.tib.orkg.prototype.contenttypes.services.actions.paper.PaperAction.State

class PaperOrganizationValidator(
    organizationRepository: PostgresOrganizationRepository
) : OrganizationValidator(organizationRepository), PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: State): State {
        validate(command.organizations)
        return state
    }
}
