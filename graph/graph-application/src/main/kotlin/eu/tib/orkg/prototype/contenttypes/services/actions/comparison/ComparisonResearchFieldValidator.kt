package eu.tib.orkg.prototype.contenttypes.services.actions.comparison

import eu.tib.orkg.prototype.contenttypes.services.actions.CreateComparisonCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.ResearchFieldValidator
import eu.tib.orkg.prototype.contenttypes.services.actions.comparison.ComparisonAction.*
import eu.tib.orkg.prototype.statements.spi.ResourceRepository

class ComparisonResearchFieldValidator(
    resourceRepository: ResourceRepository
) : ResearchFieldValidator(resourceRepository), ComparisonAction {
    override operator fun invoke(command: CreateComparisonCommand, state: State): State {
        validate(command.researchFields)
        return state
    }
}
