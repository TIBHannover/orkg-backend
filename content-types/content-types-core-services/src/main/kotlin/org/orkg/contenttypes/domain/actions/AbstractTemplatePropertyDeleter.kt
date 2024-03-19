package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class AbstractTemplatePropertyDeleter(
    private val resourceService: ResourceUseCases,
    private val statementService: StatementUseCases
) {
    internal fun delete(contributorId: ContributorId, templateId: ThingId, propertyId: ThingId) {
        val incomingStatements = statementService.findAll(
            objectId = propertyId,
            pageable = PageRequests.ALL
        ).content
        if (incomingStatements.isNotEmpty() && incomingStatements.all { it.subject.id == templateId }) {
            val outgoingStatements = statementService.findAll(
                subjectId = propertyId,
                pageable = PageRequests.ALL
            )
            statementService.delete(outgoingStatements.map { it.id }.toSet() + incomingStatements.single().id)
            try {
                resourceService.delete(propertyId, contributorId)
            } catch (e: Exception) {
                // ignore
            }
        } else {
            val statements = incomingStatements.filter { it.subject.id == templateId }.map { it.id }
            if (statements.isNotEmpty()) {
                statementService.delete(statements.toSet())
            }
        }
    }
}
