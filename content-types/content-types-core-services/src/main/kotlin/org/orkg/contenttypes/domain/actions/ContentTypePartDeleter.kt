package org.orkg.contenttypes.domain.actions

import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.input.StatementUseCases

class ContentTypePartDeleter(
    private val statementService: StatementUseCases
) {
    internal fun delete(
        contentTypeId: ThingId,
        partId: ThingId,
        delete: (List<GeneralStatement>) -> Unit
    ) {
        val incomingStatements = statementService.findAll(
            objectId = partId,
            pageable = PageRequests.ALL
        ).content
        if (incomingStatements.isOnlyReferencedByContentType(contentTypeId)) {
            delete(incomingStatements)
        } else {
            unlinkContentType(incomingStatements, contentTypeId)
        }
    }

    private fun unlinkContentType(incomingStatements: List<GeneralStatement>, contentTypeId: ThingId) {
        val toRemove = incomingStatements.filter { it.subject.id == contentTypeId }.map { it.id }
        if (toRemove.isNotEmpty()) {
            statementService.deleteAllById(toRemove.toSet())
        }
    }

    private fun MutableList<GeneralStatement>.isOnlyReferencedByContentType(contentTypeId: ThingId) =
        isNotEmpty() && all { it.subject.id == contentTypeId }
}
