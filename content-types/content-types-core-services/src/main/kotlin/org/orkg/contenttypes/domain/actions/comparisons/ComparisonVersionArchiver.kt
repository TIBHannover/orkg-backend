package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.PublishedComparison
import org.orkg.contenttypes.domain.actions.PublishComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.PublishComparisonAction.State
import org.orkg.contenttypes.output.ComparisonPublishedRepository

class ComparisonVersionArchiver(
    private val comparisonPublishedRepository: ComparisonPublishedRepository
) : PublishComparisonAction {
    override fun invoke(command: PublishComparisonCommand, state: State): State {
        comparisonPublishedRepository.save(PublishedComparison(command.id, state.config!!, state.data!!))
        return state
    }
}
