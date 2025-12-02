package org.orkg.contenttypes.domain.actions.comparisons

import dev.forkhandles.values.ofOrNull
import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedFigureNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedFigureNotModifiable
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateComparisonRelatedFigureCommand
import org.orkg.contenttypes.input.ComparisonRelatedFigureUseCases
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Description
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.Label
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class ComparisonRelatedFigureUpdater(
    private val comparisonRelatedFigureUseCases: ComparisonRelatedFigureUseCases,
    private val resourceService: ResourceUseCases,
    private val statementService: StatementUseCases,
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater,
) {
    constructor(
        comparisonRelatedFigureUseCases: ComparisonRelatedFigureUseCases,
        resourceService: ResourceUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        comparisonRelatedFigureUseCases,
        resourceService,
        statementService,
        SingleStatementPropertyUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases)
    )

    fun execute(command: UpdateComparisonRelatedFigureCommand) {
        // command.label is validated by the resource service below
        command.image?.let { Label.ofOrNull(it) ?: throw InvalidLabel("image") }
        command.description?.let { Description.ofOrNull(it) ?: throw InvalidDescription() }
        resourceService.findById(command.comparisonId)
            .filter {
                if (Classes.comparisonPublished in it.classes) {
                    throw ComparisonRelatedFigureNotModifiable(command.comparisonRelatedFigureId)
                }
                Classes.comparison in it.classes
            }
            .orElseThrow { ComparisonNotFound(command.comparisonId) }
        val comparisonRelatedFigure = comparisonRelatedFigureUseCases.findByIdAndComparisonId(command.comparisonId, command.comparisonRelatedFigureId)
            .orElseThrow { ComparisonRelatedFigureNotFound(command.comparisonRelatedFigureId) }
        if (command.label != null && command.label != comparisonRelatedFigure.label) {
            resourceService.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = comparisonRelatedFigure.id,
                    contributorId = command.contributorId,
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
