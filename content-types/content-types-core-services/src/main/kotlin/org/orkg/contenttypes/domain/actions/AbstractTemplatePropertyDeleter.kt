package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class AbstractTemplatePropertyDeleter(
    private val resourceService: ResourceUseCases,
    private val statementService: StatementUseCases,
    private val contentTypePartDeleter: ContentTypePartDeleter,
) {
    constructor(
        resourceService: ResourceUseCases,
        statementService: StatementUseCases,
    ) : this(resourceService, statementService, ContentTypePartDeleter(statementService))

    internal fun delete(contributorId: ContributorId, templateId: ThingId, propertyId: ThingId) =
        contentTypePartDeleter.delete(templateId, propertyId) { incomingStatements ->
            val outgoingStatements = statementService.findAll(
                subjectId = propertyId,
                pageable = PageRequests.ALL
            )
            statementService.deleteAllById(outgoingStatements.map { it.id }.toSet() + incomingStatements.single().id)
            resourceService.tryDelete(propertyId, contributorId)
        }
}
