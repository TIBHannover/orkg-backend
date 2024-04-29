package org.orkg.contenttypes.domain.actions.templates

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.CreateTemplateAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

class TemplateRelationsCreator(
    private val statementUseCases: StatementUseCases
) : CreateTemplateAction {
    override fun invoke(command: CreateTemplateCommand, state: State): State {
        linkResearchFields(command.contributorId, state.templateId!!, command.relations.researchFields)
        linkResearchProblems(command.contributorId, state.templateId, command.relations.researchProblems)
        command.relations.predicate?.let { predicateId ->
            linkPredicate(command.contributorId, state.templateId, predicateId)
        }
        return state
    }

    internal fun linkResearchFields(
        contributorId: ContributorId,
        subjectId: ThingId,
        researchFields: List<ThingId>
    ) {
        researchFields.forEach { researchFieldId ->
            statementUseCases.add(
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.templateOfResearchField,
                `object` = researchFieldId
            )
        }
    }

    internal fun linkResearchProblems(
        contributorId: ContributorId,
        subjectId: ThingId,
        researchProblems: List<ThingId>
    ) {
        researchProblems.forEach { researchProblemId ->
            statementUseCases.add(
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.templateOfResearchProblem,
                `object` = researchProblemId
            )
        }
    }

    internal fun linkPredicate(
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId
    ) {
        statementUseCases.add(
            userId = contributorId,
            subject = subjectId,
            predicate = Predicates.templateOfPredicate,
            `object` = predicateId
        )
    }
}
