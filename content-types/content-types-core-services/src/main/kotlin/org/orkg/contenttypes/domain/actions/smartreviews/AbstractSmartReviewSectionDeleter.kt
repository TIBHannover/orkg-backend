package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.SmartReviewSection
import org.orkg.contenttypes.domain.actions.ContentTypePartDeleter
import org.orkg.contenttypes.domain.actions.tryDelete
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class AbstractSmartReviewSectionDeleter(
    private val statementService: StatementUseCases,
    private val resourceService: ResourceUseCases,
    private val contentTypePartDeleter: ContentTypePartDeleter
) {
    constructor(
        statementService: StatementUseCases,
        resourceService: ResourceUseCases
    ) : this(statementService, resourceService, ContentTypePartDeleter(statementService))

    internal fun delete(
        contributorId: ContributorId,
        contributionId: ThingId,
        section: SmartReviewSection,
        statements: Map<ThingId, List<GeneralStatement>>
    ) {
        contentTypePartDeleter.delete(contributionId, section.id) { incomingStatements ->
            val toRemove = statements[section.id]?.map { it.id }.orEmpty() union incomingStatements.map { it.id }
            if (toRemove.isNotEmpty()) {
                statementService.delete(toRemove)
            }
            resourceService.tryDelete(section.id, contributorId)
        }
    }
}
