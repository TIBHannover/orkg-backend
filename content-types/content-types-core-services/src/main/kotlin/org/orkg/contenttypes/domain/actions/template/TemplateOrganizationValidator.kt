package org.orkg.contenttypes.domain.actions.template

import org.orkg.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.OrganizationValidator
import org.orkg.contenttypes.domain.actions.template.TemplateAction.State

class TemplateOrganizationValidator(
    organizationRepository: PostgresOrganizationRepository
) : OrganizationValidator(organizationRepository), TemplateAction {
    override operator fun invoke(command: CreateTemplateCommand, state: State): State =
        state.apply { validate(command.organizations) }
}
