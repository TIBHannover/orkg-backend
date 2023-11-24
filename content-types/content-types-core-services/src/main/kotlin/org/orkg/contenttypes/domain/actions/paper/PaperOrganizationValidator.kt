package org.orkg.contenttypes.domain.actions.paper

import org.orkg.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.OrganizationValidator
import org.orkg.contenttypes.domain.actions.paper.PaperAction.State

class PaperOrganizationValidator(
    organizationRepository: PostgresOrganizationRepository
) : OrganizationValidator(organizationRepository), PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: State): State {
        validate(command.organizations)
        return state
    }
}
