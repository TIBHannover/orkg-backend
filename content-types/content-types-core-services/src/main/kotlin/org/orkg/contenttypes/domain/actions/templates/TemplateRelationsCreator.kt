package org.orkg.contenttypes.domain.actions.templates

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.CreateTemplateAction.State
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateStatementUseCase.CreateCommand
import org.orkg.graph.input.UnsafeStatementUseCases

class TemplateRelationsCreator(
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
) : CreateTemplateAction {
    override fun invoke(command: CreateTemplateCommand, state: State): State {
        linkResearchFields(command.contributorId, state.templateId!!, command.relations.researchFields, command.extractionMethod)
        linkResearchProblems(command.contributorId, state.templateId, command.relations.researchProblems, command.extractionMethod)
        command.relations.predicate?.also { predicateId ->
            linkPredicate(command.contributorId, state.templateId, predicateId, command.extractionMethod)
        }
        return state
    }

    private fun linkResearchFields(
        contributorId: ContributorId,
        subjectId: ThingId,
        researchFields: List<ThingId>,
        extractionMethod: ExtractionMethod,
    ) {
        researchFields.forEach { researchFieldId ->
            unsafeStatementUseCases.create(
                CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.templateOfResearchField,
                    objectId = researchFieldId,
                    extractionMethod = extractionMethod,
                ),
            )
        }
    }

    private fun linkResearchProblems(
        contributorId: ContributorId,
        subjectId: ThingId,
        researchProblems: List<ThingId>,
        extractionMethod: ExtractionMethod,
    ) {
        researchProblems.forEach { researchProblemId ->
            unsafeStatementUseCases.create(
                CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.templateOfResearchProblem,
                    objectId = researchProblemId,
                    extractionMethod = extractionMethod,
                ),
            )
        }
    }

    private fun linkPredicate(
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        extractionMethod: ExtractionMethod,
    ) {
        unsafeStatementUseCases.create(
            CreateCommand(
                contributorId = contributorId,
                subjectId = subjectId,
                predicateId = Predicates.templateOfPredicate,
                objectId = predicateId,
                extractionMethod = extractionMethod,
            ),
        )
    }
}
