package eu.tib.orkg.prototype.contenttypes.services.actions.comparison

import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.contenttypes.services.actions.CreateComparisonCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.OrganizationValidator
import eu.tib.orkg.prototype.contenttypes.services.actions.comparison.ComparisonAction.State

class ComparisonOrganizationValidator(
    organizationRepository: PostgresOrganizationRepository
) : OrganizationValidator(organizationRepository), ComparisonAction {
    override operator fun invoke(command: CreateComparisonCommand, state: State): State {
        validate(command.organizations)
        return state
    }
}
