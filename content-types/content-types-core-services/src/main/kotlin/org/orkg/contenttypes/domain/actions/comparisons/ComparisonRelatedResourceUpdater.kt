package org.orkg.contenttypes.domain.actions.comparisons

import dev.forkhandles.values.ofOrNull
import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedResourceNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedResourceNotModifiable
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateComparisonRelatedResourceCommand
import org.orkg.contenttypes.input.ComparisonRelatedResourceUseCases
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

class ComparisonRelatedResourceUpdater(
    private val comparisonRelatedResourceUseCases: ComparisonRelatedResourceUseCases,
    private val resourceService: ResourceUseCases,
    private val statementService: StatementUseCases,
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater,
) {
    constructor(
        comparisonRelatedResourceUseCases: ComparisonRelatedResourceUseCases,
        resourceService: ResourceUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        comparisonRelatedResourceUseCases,
        resourceService,
        statementService,
        SingleStatementPropertyUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases)
    )

    fun execute(command: UpdateComparisonRelatedResourceCommand) {
        // command.label is validated by the resource service below
        command.image?.let { Label.ofOrNull(it) ?: throw InvalidLabel("image") }
        command.url?.let { Label.ofOrNull(it) ?: throw InvalidLabel("url") }
        command.description?.let { Description.ofOrNull(it) ?: throw InvalidDescription() }
        resourceService.findById(command.comparisonId)
            .filter {
                if (Classes.comparisonPublished in it.classes) {
                    throw ComparisonRelatedResourceNotModifiable(command.comparisonRelatedResourceId)
                }
                Classes.comparison in it.classes
            }
            .orElseThrow { ComparisonNotFound(command.comparisonId) }
        val comparisonRelatedResource = comparisonRelatedResourceUseCases.findByIdAndComparisonId(command.comparisonId, command.comparisonRelatedResourceId)
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
