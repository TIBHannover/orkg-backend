package org.orkg.contenttypes.domain.actions.comparison

import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.ResearchFieldValidator
import org.orkg.contenttypes.domain.actions.comparison.ComparisonAction.State
import org.orkg.graph.output.ResourceRepository

class ComparisonResearchFieldValidator(
    resourceRepository: ResourceRepository
) : ResearchFieldValidator(resourceRepository), ComparisonAction {
    override operator fun invoke(command: CreateComparisonCommand, state: State): State {
        validate(command.researchFields)
        return state
    }
}
