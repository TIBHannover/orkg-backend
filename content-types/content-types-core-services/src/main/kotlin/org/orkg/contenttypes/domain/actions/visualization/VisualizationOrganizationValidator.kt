package org.orkg.contenttypes.domain.actions.visualization

import org.orkg.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import org.orkg.contenttypes.domain.actions.CreateVisualizationCommand
import org.orkg.contenttypes.domain.actions.OrganizationValidator
import org.orkg.contenttypes.domain.actions.visualization.VisualizationAction.State

class VisualizationOrganizationValidator(
    organizationRepository: PostgresOrganizationRepository
) : OrganizationValidator(organizationRepository), VisualizationAction {
    override operator fun invoke(command: CreateVisualizationCommand, state: State): State {
        validate(command.organizations)
        return state
    }
}
