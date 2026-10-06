package org.orkg.contenttypes.domain.actions.comparisons.resources

import dev.forkhandles.values.ofOrNull
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.actions.CreateComparisonRelatedResourceCommand
import org.orkg.contenttypes.domain.actions.comparisons.resources.CreateComparisonRelatedResourceAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Description
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.Label
import org.orkg.graph.output.ResourceRepository

class ComparisonRelatedResourceValidator(
    private val resourceRepository: ResourceRepository,
) : CreateComparisonRelatedResourceAction {
    override fun invoke(command: CreateComparisonRelatedResourceCommand, state: State): State {
        Label.ofOrNull(command.label) ?: throw InvalidLabel()
        command.image?.let { Label.ofOrNull(it) ?: throw InvalidLabel("image") }
        command.url?.let { Label.ofOrNull(it) ?: throw InvalidLabel("url") }
        command.description?.let { Description.ofOrNull(it) ?: throw InvalidDescription() }
        resourceRepository.findById(command.comparisonId)
            .filter { Classes.comparison in it.classes }
            .orElseThrow { ComparisonNotFound(command.comparisonId) }
        return state
    }
}
