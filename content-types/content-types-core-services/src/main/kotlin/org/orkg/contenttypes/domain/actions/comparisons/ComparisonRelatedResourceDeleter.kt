package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonRelatedResourceNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedResourceNotModifiable
import org.orkg.contenttypes.domain.actions.ContentTypePartDeleter
import org.orkg.contenttypes.domain.actions.tryDelete
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class ComparisonRelatedResourceDeleter(
    private val statementService: StatementUseCases,
    private val resourceService: ResourceUseCases,
    private val contentTypePartDeleter: ContentTypePartDeleter,
) {
    constructor(
        statementService: StatementUseCases,
        resourceService: ResourceUseCases
    ) : this(
        statementService,
        resourceService,
        ContentTypePartDeleter(statementService)
    )

    fun execute(comparisonId: ThingId, comparisonRelatedResourceId: ThingId, contributorId: ContributorId) {
        val hasRelatedResourceStatements = statementService.findAll(
            subjectId = comparisonId,
            predicateId = Predicates.hasRelatedResource,
            objectId = comparisonRelatedResourceId,
            objectClasses = setOf(Classes.comparisonRelatedResource),
            pageable = PageRequests.SINGLE
        )
        if (hasRelatedResourceStatements.isEmpty) {
            throw ComparisonRelatedResourceNotFound(comparisonRelatedResourceId)
        }
        val comparisonRelatedResource = hasRelatedResourceStatements.single().`object` as Resource
        val isPreviousVersionComparison = statementService.findAll(
            subjectClasses = setOf(Classes.comparison),
            predicateId = Predicates.hasPreviousVersion,
            objectId = comparisonId,
            pageable = PageRequests.SINGLE
        )
        if (!isPreviousVersionComparison.isEmpty || !comparisonRelatedResource.modifiable) {
            throw ComparisonRelatedResourceNotModifiable(comparisonId)
        }

        contentTypePartDeleter.delete(comparisonId, comparisonRelatedResourceId) { incomingStatements ->
            val statementToRemove = statementService.findAll(
                subjectId = comparisonRelatedResourceId,
                pageable = PageRequests.ALL
            ).content + incomingStatements

            if (statementToRemove.isNotEmpty()) {
                statementService.delete(statementToRemove.map { it.id }.toSet())
            }

            resourceService.tryDelete(comparisonRelatedResource.id, contributorId)
        }
    }
}
