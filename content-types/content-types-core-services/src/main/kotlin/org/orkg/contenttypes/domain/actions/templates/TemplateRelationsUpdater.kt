package org.orkg.contenttypes.domain.actions.templates

import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.UpdateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.UpdateTemplateAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

class TemplateRelationsUpdater(
    private val statementUseCases: StatementUseCases,
    private val templateRelationsCreator: TemplateRelationsCreator = TemplateRelationsCreator(statementUseCases)
) : UpdateTemplateAction {
    override fun invoke(command: UpdateTemplateCommand, state: State): State {
        if (command.relations != null) {
            val oldResearchFields = state.template?.relations?.researchFields?.map { it.id }.orEmpty().toSet()
            val newResearchFields = command.relations!!.researchFields.toSet()
            if (oldResearchFields != newResearchFields) {
                updateOrLinkResearchFields(
                    oldResearchFields = oldResearchFields,
                    newResearchFields = newResearchFields,
                    contributorId = command.contributorId,
                    subjectId = command.templateId
                )
            }

            val oldResearchProblems = state.template?.relations?.researchProblems?.map { it.id }.orEmpty().toSet()
            val newResearchProblems = command.relations!!.researchProblems.toSet()
            if (oldResearchProblems != newResearchProblems) {
                updateOrLinkResearchProblems(
                    oldResearchProblems = oldResearchProblems,
                    newResearchProblems = newResearchProblems,
                    contributorId = command.contributorId,
                    subjectId = command.templateId
                )
            }

            if (state.template?.relations?.predicate?.id != command.relations?.predicate) {
                updateOrLinkPredicate(
                    oldPredicate = state.template?.relations?.predicate?.id,
                    newPredicate = command.relations!!.predicate,
                    contributorId = command.contributorId,
                    subjectId = command.templateId
                )
            }
        }
        return state
    }

    private fun updateOrLinkResearchFields(
        oldResearchFields: Set<ThingId>,
        newResearchFields: Set<ThingId>,
        contributorId: ContributorId,
        subjectId: ThingId
    ) {
        val toRemove = oldResearchFields - newResearchFields
        val toAdd = newResearchFields - oldResearchFields

        if (toRemove.isNotEmpty()) {
            val statements = statementUseCases.findAll(
                subjectId = subjectId,
                predicateId = Predicates.templateOfResearchField,
                objectClasses = setOf(Classes.researchField),
                pageable = PageRequests.ALL
            ).content.filter { it.`object`.id in toRemove }

            if (statements.isNotEmpty()) {
                statementUseCases.delete(statements.map { it.id }.toSet())
            }
        }

        if (toAdd.isNotEmpty()) {
            templateRelationsCreator.linkResearchFields(contributorId, subjectId, toAdd.toList())
        }
    }

    private fun updateOrLinkResearchProblems(
        oldResearchProblems: Set<ThingId>,
        newResearchProblems: Set<ThingId>,
        contributorId: ContributorId,
        subjectId: ThingId
    ) {
        val toRemove = oldResearchProblems - newResearchProblems
        val toAdd = newResearchProblems - oldResearchProblems

        if (toRemove.isNotEmpty()) {
            val statements = statementUseCases.findAll(
                subjectId = subjectId,
                predicateId = Predicates.templateOfResearchProblem,
                objectClasses = setOf(Classes.problem),
                pageable = PageRequests.ALL
            ).content.filter { it.`object`.id in toRemove }

            if (statements.isNotEmpty()) {
                statementUseCases.delete(statements.map { it.id }.toSet())
            }
        }

        if (toAdd.isNotEmpty()) {
            templateRelationsCreator.linkResearchProblems(contributorId, subjectId, toAdd.toList())
        }
    }

    private fun updateOrLinkPredicate(
        oldPredicate: ThingId?,
        newPredicate: ThingId?,
        contributorId: ContributorId,
        subjectId: ThingId
    ) {
        if (oldPredicate != null) {
            val statement = statementUseCases.findAll(
                subjectId = subjectId,
                predicateId = Predicates.templateOfPredicate,
                objectClasses = setOf(Classes.predicate),
                pageable = PageRequests.SINGLE
            ).single { it.`object` is Predicate }
            statementUseCases.delete(statement.id)
        }

        if (newPredicate != null) {
            templateRelationsCreator.linkPredicate(contributorId, subjectId, newPredicate)
        }
    }
}
