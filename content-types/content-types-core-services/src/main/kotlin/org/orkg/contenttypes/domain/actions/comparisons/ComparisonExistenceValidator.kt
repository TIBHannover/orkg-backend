package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.Comparison
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.ComparisonNotModifiable
import org.orkg.contenttypes.domain.ComparisonService
import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.UpdateComparisonAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository

class ComparisonExistenceValidator(
    private val comparisonService: ComparisonService,
    private val resourceRepository: ResourceRepository,
) : UpdateComparisonAction {
    override fun invoke(command: UpdateComparisonCommand, state: State): State {
        val resource = resourceRepository.findById(command.comparisonId)
            .filter {
                if (Classes.comparisonPublished in it.classes) {
                    throw ComparisonNotModifiable(it.id)
                }
                Classes.comparison in it.classes
            }
            .orElseThrow { ComparisonNotFound(command.comparisonId) }
        val subgraph = comparisonService.findSubgraph(resource)
        val table = with(comparisonService) { resource.findTableData() }
        val versionInfo = with(comparisonService) { resource.findVersionInfo(subgraph.statements) }
        val comparison = Comparison.from(
            resource = resource,
            statements = subgraph.statements,
            table = table,
            versionInfo = versionInfo
        )
        return state.copy(comparison = comparison, statements = subgraph.statements)
    }
}
