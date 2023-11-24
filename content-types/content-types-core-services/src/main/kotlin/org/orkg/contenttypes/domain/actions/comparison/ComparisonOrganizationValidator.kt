package org.orkg.contenttypes.domain.actions.comparison

import org.orkg.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.OrganizationValidator
import org.orkg.contenttypes.domain.actions.comparison.ComparisonAction.State

class ComparisonOrganizationValidator(
    organizationRepository: PostgresOrganizationRepository
) : OrganizationValidator(organizationRepository), ComparisonAction {
    override operator fun invoke(command: CreateComparisonCommand, state: State): State {
        validate(command.organizations)
        return state
    }
}
