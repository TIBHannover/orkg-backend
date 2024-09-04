package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonRelatedFigureNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedFigureNotModifiable
import org.orkg.contenttypes.domain.actions.ContentTypePartDeleter
import org.orkg.contenttypes.domain.actions.tryDelete
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class ComparisonRelatedFigureDeleter(
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

    fun execute(comparisonId: ThingId, comparisonRelatedFigureId: ThingId, contributorId: ContributorId) {
        val hasRelatedFigureStatements = statementService.findAll(
            subjectId = comparisonId,
            predicateId = Predicates.hasRelatedFigure,
            objectId = comparisonRelatedFigureId,
            objectClasses = setOf(Classes.comparisonRelatedFigure),
            pageable = PageRequests.SINGLE
        )
        if (hasRelatedFigureStatements.isEmpty) {
            throw ComparisonRelatedFigureNotFound(comparisonRelatedFigureId)
        }
        val comparisonRelatedFigure = hasRelatedFigureStatements.single().`object` as Resource
        val isPreviousVersionComparison = statementService.findAll(
            subjectClasses = setOf(Classes.comparison),
            predicateId = Predicates.hasPreviousVersion,
            objectId = comparisonId,
            pageable = PageRequests.SINGLE
        )
        if (!isPreviousVersionComparison.isEmpty || !comparisonRelatedFigure.modifiable) {
            throw ComparisonRelatedFigureNotModifiable(comparisonId)
        }

        contentTypePartDeleter.delete(comparisonId, comparisonRelatedFigureId) { incomingStatements ->
            val statementToRemove = statementService.findAll(
                subjectId = comparisonRelatedFigureId,
                pageable = PageRequests.ALL
            ).content + incomingStatements

            if (statementToRemove.isNotEmpty()) {
                statementService.delete(statementToRemove.map { it.id }.toSet())
            }

            resourceService.tryDelete(comparisonRelatedFigure.id, contributorId)
        }
    }
}
