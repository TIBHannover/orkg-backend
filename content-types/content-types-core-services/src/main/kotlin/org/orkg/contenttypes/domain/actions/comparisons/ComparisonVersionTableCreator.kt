package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.PublishedComparison
import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.CreateComparisonAction.State
import org.orkg.contenttypes.output.ComparisonPublishedRepository

class ComparisonVersionTableCreator(
    private val comparisonPublishedRepository: ComparisonPublishedRepository,
) : CreateComparisonAction {
    override fun invoke(command: CreateComparisonCommand, state: State): State {
        comparisonPublishedRepository.save(PublishedComparison(state.comparisonId!!, command.config, command.data))
        return state
    }
}
