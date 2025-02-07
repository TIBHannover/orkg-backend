package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedResourceNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedResourceNotModifiable
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.input.ComparisonUseCases
import org.orkg.contenttypes.input.UpdateComparisonUseCase.UpdateComparisonRelatedResourceCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class ComparisonRelatedResourceUpdater(
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
        unsafeStatementUseCases: UnsafeStatementUseCases
    ) : this(
        comparisonService,
        resourceService,
        statementService,
        SingleStatementPropertyUpdater(literalService, statementService, unsafeStatementUseCases)
    )

    fun execute(command: UpdateComparisonRelatedResourceCommand) {
        resourceService.findById(command.comparisonId)
            .filter {
                if (Classes.comparisonPublished in it.classes) {
                    throw ComparisonRelatedResourceNotModifiable(command.comparisonRelatedResourceId)
                }
                Classes.comparison in it.classes
            }
            .orElseThrow { ComparisonNotFound(command.comparisonId) }
        val comparisonRelatedResource = comparisonService.findRelatedResourceById(command.comparisonId, command.comparisonRelatedResourceId)
            .orElseThrow { ComparisonRelatedResourceNotFound(command.comparisonRelatedResourceId) }
        if (command.label != null && command.label != comparisonRelatedResource.label) {
            resourceService.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = comparisonRelatedResource.id,
                    contributorId = command.contributorId,
                    label = command.label
                )
            )
        }
        val statements by lazy {
            statementService.findAll(
                subjectId = command.comparisonRelatedResourceId,
                pageable = PageRequests.ALL
            ).content
        }
        if (command.image != comparisonRelatedResource.image) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.comparisonRelatedResourceId,
                predicateId = Predicates.hasImage,
                label = command.image
            )
        }
        if (command.url != comparisonRelatedResource.url) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.comparisonRelatedResourceId,
                predicateId = Predicates.hasURL,
                label = command.url
            )
        }
        if (command.description != comparisonRelatedResource.description) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.comparisonRelatedResourceId,
                predicateId = Predicates.description,
                label = command.description
            )
        }
    }
}
