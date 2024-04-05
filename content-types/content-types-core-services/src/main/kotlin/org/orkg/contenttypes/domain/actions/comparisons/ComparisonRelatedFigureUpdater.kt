package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.ComparisonRelatedFigureNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedFigureNotModifiable
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.input.ComparisonUseCases
import org.orkg.contenttypes.input.UpdateComparisonUseCase.UpdateComparisonRelatedFigureCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class ComparisonRelatedFigureUpdater(
    private val comparisonService: ComparisonUseCases,
    private val resourceService: ResourceUseCases,
    private val statementService: StatementUseCases,
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater
) {
    constructor(
        comparisonService: ComparisonUseCases,
        resourceService: ResourceUseCases,
        literalService: LiteralUseCases,
        statementService: StatementUseCases,
    ) : this(
        comparisonService,
        resourceService,
        statementService,
        SingleStatementPropertyUpdater(literalService, statementService)
    )

    fun execute(command: UpdateComparisonRelatedFigureCommand) {
        val comparisonRelatedFigure = comparisonService.findRelatedFigureById(command.comparisonId, command.comparisonRelatedFigureId)
            .orElseThrow { ComparisonRelatedFigureNotFound(command.comparisonRelatedFigureId) }
        val isPreviousVersionComparison = statementService.findAll(
            subjectClasses = setOf(Classes.comparison),
            predicateId = Predicates.hasPreviousVersion,
            objectId = command.comparisonId,
            pageable = PageRequests.SINGLE
        )
        if (!isPreviousVersionComparison.isEmpty) {
            throw ComparisonRelatedFigureNotModifiable(command.comparisonId)
        }
        if (command.label != null && command.label != comparisonRelatedFigure.label) {
            resourceService.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = comparisonRelatedFigure.id,
                    label = command.label
                )
            )
        }
        val statements by lazy {
            statementService.findAll(
                subjectId = command.comparisonRelatedFigureId,
                pageable = PageRequests.ALL
            ).content
        }
        if (command.image != comparisonRelatedFigure.image) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.comparisonRelatedFigureId,
                predicateId = Predicates.hasImage,
                label = command.image
            )
        }
        if (command.description != comparisonRelatedFigure.description) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.comparisonRelatedFigureId,
                predicateId = Predicates.description,
                label = command.description
            )
        }
    }
}
