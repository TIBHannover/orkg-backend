package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.VisualizationNotFound
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository

class ComparisonVisualizationValidator<T, S>(
    private val resourceRepository: ResourceRepository,
    private val valueSelector: (T) -> List<ThingId>?,
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        valueSelector(command)?.let { visualizations ->
            visualizations.distinct().forEach { visualizationId ->
                resourceRepository.findById(visualizationId)
                    .filter { Classes.visualization in it.classes }
                    .orElseThrow { VisualizationNotFound(visualizationId) }
            }
        }
        return state
    }
}
