package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

abstract class ResearchFieldUpdater(
    private val statementService: StatementUseCases,
    private val researchFieldCreator: ResearchFieldCreator = object : ResearchFieldCreator(statementService) {}
) {
    internal fun update(contributorId: ContributorId, researchFields: List<ThingId>, subjectId: ThingId) {
        // Find out what already exists and what needs to be created or removed
        val oldResearchFields = statementService.findAll(
            subjectId = subjectId,
            predicateId = Predicates.hasResearchField,
            pageable = PageRequests.ALL
        ).associate { it.`object`.id to it.id }
        val toRemove = oldResearchFields.keys - researchFields.toSet()
        val toAdd = researchFields - oldResearchFields.keys.toSet()

        // Remove unwanted research fields
        toRemove.forEach { researchFieldId ->
            statementService.delete(oldResearchFields[researchFieldId]!!)
        }

        // Create new research fields
        if (toAdd.isNotEmpty()) {
            researchFieldCreator.create(contributorId, toAdd, subjectId)
        }
    }
}
