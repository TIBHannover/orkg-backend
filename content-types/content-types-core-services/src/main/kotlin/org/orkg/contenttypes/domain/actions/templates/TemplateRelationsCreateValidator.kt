package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.ResearchFieldNotFound
import org.orkg.contenttypes.domain.ResearchProblemNotFound
import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.CreateTemplateAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository

class TemplateRelationsCreateValidator(
    private val resourceRepository: ResourceRepository,
    private val predicateRepository: PredicateRepository,
) : CreateTemplateAction {
    override fun invoke(command: CreateTemplateCommand, state: State): State {
        command.relations.researchFields.forEach { researchFieldId ->
            resourceRepository.findById(researchFieldId)
                .filter { Classes.researchField in it.classes }
                .orElseThrow { ResearchFieldNotFound(researchFieldId) }
        }
        command.relations.researchProblems.forEach { researchProblemId ->
            resourceRepository.findById(researchProblemId)
                .filter { Classes.problem in it.classes }
                .orElseThrow { ResearchProblemNotFound(researchProblemId) }
        }
        command.relations.predicate?.also {
            predicateRepository.findById(it).orElseThrow { PredicateNotFound(it) }
        }
        return state
    }
}
