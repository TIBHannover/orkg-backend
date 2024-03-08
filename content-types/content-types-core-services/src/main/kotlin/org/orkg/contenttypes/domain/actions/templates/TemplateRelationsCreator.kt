package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.TemplateAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

class TemplateRelationsCreator(
    private val statementUseCases: StatementUseCases
) : TemplateAction {
    override fun invoke(command: CreateTemplateCommand, state: State): State {
        command.relations.researchFields.forEach { researchFieldId ->
            statementUseCases.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.templateOfResearchField,
                `object` = researchFieldId
            )
        }
        command.relations.researchProblems.forEach { researchProblemId ->
            statementUseCases.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.templateOfResearchProblem,
                `object` = researchProblemId
            )
        }
        command.relations.predicate?.let { predicateId ->
            statementUseCases.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.templateOfPredicate,
                `object` = predicateId
            )
        }
        return state
    }
}
