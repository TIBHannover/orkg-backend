package org.orkg.contenttypes.domain.actions.paper

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.ResearchFieldValidator
import org.orkg.contenttypes.domain.actions.paper.PaperAction.State
import org.orkg.graph.output.ResourceRepository

class PaperResearchFieldValidator(
    resourceRepository: ResourceRepository
) : ResearchFieldValidator(resourceRepository), PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: State): State {
        validate(command.researchFields)
        return state
    }
}
