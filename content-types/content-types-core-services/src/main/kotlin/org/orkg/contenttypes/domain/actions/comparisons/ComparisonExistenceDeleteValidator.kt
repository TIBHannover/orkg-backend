package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.ComparisonService
import org.orkg.contenttypes.domain.actions.DeleteComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.DeleteComparisonAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository

class ComparisonExistenceDeleteValidator(
    private val comparisonService: ComparisonService,
    private val resourceRepository: ResourceRepository,
) : DeleteComparisonAction {
    override fun invoke(command: DeleteComparisonCommand, state: State): State {
        val resource = resourceRepository.findById(command.comparisonId)
            .filter { Classes.comparison in it.classes || Classes.comparisonPublished in it.classes }
            .orElse(null)
        val subgraph = resource?.let(comparisonService::findSubgraph)
        return state.copy(comparison = resource, statements = subgraph?.statements.orEmpty())
    }
}
