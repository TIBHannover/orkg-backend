package org.orkg.contenttypes.domain.actions.templates

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.CreateTemplateAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateStatementUseCase.CreateCommand
import org.orkg.graph.input.UnsafeStatementUseCases

class TemplateRelationsCreator(
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
) : CreateTemplateAction {
    override fun invoke(command: CreateTemplateCommand, state: State): State {
        linkResearchFields(command.contributorId, state.templateId!!, command.relations.researchFields)
        linkResearchProblems(command.contributorId, state.templateId, command.relations.researchProblems)
        command.relations.predicate?.let { predicateId ->
            linkPredicate(command.contributorId, state.templateId, predicateId)
        }
        return state
    }

    private fun linkResearchFields(
        contributorId: ContributorId,
        subjectId: ThingId,
        researchFields: List<ThingId>,
    ) {
        researchFields.forEach { researchFieldId ->
            unsafeStatementUseCases.create(
                CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.templateOfResearchField,
                    objectId = researchFieldId
                )
            )
        }
    }

    private fun linkResearchProblems(
        contributorId: ContributorId,
        subjectId: ThingId,
        researchProblems: List<ThingId>,
    ) {
        researchProblems.forEach { researchProblemId ->
            unsafeStatementUseCases.create(
                CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.templateOfResearchProblem,
                    objectId = researchProblemId
                )
            )
        }
    }

    private fun linkPredicate(
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
    ) {
        unsafeStatementUseCases.create(
            CreateCommand(
                contributorId = contributorId,
                subjectId = subjectId,
                predicateId = Predicates.templateOfPredicate,
                objectId = predicateId
            )
        )
    }
}
