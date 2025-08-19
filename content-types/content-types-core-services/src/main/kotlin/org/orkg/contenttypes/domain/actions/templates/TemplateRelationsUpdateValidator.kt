package org.orkg.contenttypes.domain.actions.templates

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ResearchFieldNotFound
import org.orkg.contenttypes.domain.ResearchProblemNotFound
import org.orkg.contenttypes.domain.actions.UpdateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.UpdateTemplateAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository

class TemplateRelationsUpdateValidator(
    private val resourceRepository: ResourceRepository,
    private val predicateRepository: PredicateRepository,
) : UpdateTemplateAction {
    override fun invoke(command: UpdateTemplateCommand, state: State): State {
        command.relations?.also { relations ->
            (relations.researchFields - state.researchFields).forEach { researchFieldId ->
                resourceRepository.findById(researchFieldId)
                    .filter { Classes.researchField in it.classes }
                    .orElseThrow { ResearchFieldNotFound(researchFieldId) }
            }
            (relations.researchProblems - state.researchProblems).forEach { researchProblemId ->
                resourceRepository.findById(researchProblemId)
                    .filter { Classes.problem in it.classes }
                    .orElseThrow { ResearchProblemNotFound(researchProblemId) }
            }
            relations.predicate?.also { predicate ->
                if (predicate != state.template!!.relations.predicate?.id) {
                    predicateRepository.findById(predicate).orElseThrow { PredicateNotFound(predicate) }
                }
            }
        }
        return state
    }

    private inline val State.researchFields: Set<ThingId>
        get() = template!!.relations.researchFields.map { it.id }.toSet()

    private inline val State.researchProblems: Set<ThingId>
        get() = template!!.relations.researchProblems.map { it.id }.toSet()
}
