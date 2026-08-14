package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonAlreadyPublished
import org.orkg.contenttypes.domain.ComparisonInUse
import org.orkg.contenttypes.domain.ComparisonNotModifiable
import org.orkg.contenttypes.domain.actions.DeleteComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.DeleteComparisonAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.output.ThingRepository

class ComparisonDeleteValidator(
    private val thingRepository: ThingRepository,
) : DeleteComparisonAction {
    override fun invoke(command: DeleteComparisonCommand, state: State): State {
        if (state.comparison != null) {
            if (!state.comparison.modifiable) {
                throw ComparisonNotModifiable(command.comparisonId)
            }
            if (Classes.comparisonPublished in state.comparison.classes || state.comparison.hasPublishedVersions(state.statements)) {
                throw ComparisonAlreadyPublished(command.comparisonId)
            }
            if (thingRepository.isUsedAsObject(command.comparisonId)) {
                throw ComparisonInUse(command.comparisonId)
            }
        }
        return state
    }

    private fun Resource.hasPublishedVersions(statements: Map<ThingId, List<GeneralStatement>>): Boolean =
        statements[id].orEmpty().any {
            it.predicate.id == Predicates.hasPublishedVersion &&
                it.`object` is Resource && Classes.comparisonPublished in (it.`object` as Resource).classes
        }
}
