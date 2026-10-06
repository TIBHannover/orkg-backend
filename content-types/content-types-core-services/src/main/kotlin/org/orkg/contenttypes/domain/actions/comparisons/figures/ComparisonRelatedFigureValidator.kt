package org.orkg.contenttypes.domain.actions.comparisons.figures

import dev.forkhandles.values.ofOrNull
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.actions.CreateComparisonRelatedFigureCommand
import org.orkg.contenttypes.domain.actions.comparisons.figures.CreateComparisonRelatedFigureAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Description
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.Label
import org.orkg.graph.output.ResourceRepository

class ComparisonRelatedFigureValidator(
    private val resourceRepository: ResourceRepository,
) : CreateComparisonRelatedFigureAction {
    override fun invoke(command: CreateComparisonRelatedFigureCommand, state: State): State {
        Label.ofOrNull(command.label) ?: throw InvalidLabel()
        command.image?.let { Label.ofOrNull(it) ?: throw InvalidLabel("image") }
        command.description?.let { Description.ofOrNull(it) ?: throw InvalidDescription() }
        resourceRepository.findById(command.comparisonId)
            .filter { Classes.comparison in it.classes }
            .orElseThrow { ComparisonNotFound(command.comparisonId) }
        return state
    }
}
