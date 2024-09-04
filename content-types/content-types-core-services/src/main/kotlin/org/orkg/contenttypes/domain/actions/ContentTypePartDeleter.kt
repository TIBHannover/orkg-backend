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
        if (incomingStatements.isNotEmpty() && incomingStatements.all { it.subject.id == contentTypeId }) {
            // part is only referenced by specified content-type, part can be deleted fully
            delete(incomingStatements)
        } else {
            // part is referenced by another entity, unlink
            val toRemove = incomingStatements.filter { it.subject.id == contentTypeId }.map { it.id }
            if (toRemove.isNotEmpty()) {
                statementService.delete(toRemove.toSet())
            }
        }
    }
}
