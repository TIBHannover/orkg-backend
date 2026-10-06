package org.orkg.contenttypes.domain

import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.CreateComparisonRelatedFigureCommand
import org.orkg.contenttypes.domain.actions.CreateComparisonRelatedFigureState
import org.orkg.contenttypes.domain.actions.UpdateComparisonRelatedFigureCommand
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonRelatedFigureDeleter
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonRelatedFigureUpdater
import org.orkg.contenttypes.domain.actions.comparisons.figures.ComparisonRelatedFigureCreator
import org.orkg.contenttypes.domain.actions.comparisons.figures.ComparisonRelatedFigureValidator
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.input.ComparisonRelatedFigureUseCases
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.Optional

@Service
@TransactionalOnNeo4j
class ComparisonRelatedFigureService(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val resourceService: ResourceUseCases,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val statementService: StatementUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
) : ComparisonRelatedFigureUseCases {
    override fun findByIdAndComparisonId(comparisonId: ThingId, id: ThingId): Optional<ComparisonRelatedFigure> =
        statementRepository.findAll(
            subjectId = comparisonId,
            predicateId = Predicates.hasRelatedFigure,
            objectId = id,
            pageable = PageRequests.SINGLE,
        )
            .filter { it.`object` is Resource && Classes.comparisonRelatedFigure in (it.`object` as Resource).classes }
            .singleOrNull()
            .let { Optional.ofNullable(it) }
            .map { (it.`object` as Resource).toComparisonRelatedFigure() }

    override fun findAllByComparisonId(comparisonId: ThingId, pageable: Pageable): Page<ComparisonRelatedFigure> =
        statementRepository.findAll(subjectId = comparisonId, predicateId = Predicates.hasRelatedFigure, pageable = pageable)
            .map { (it.`object` as Resource).toComparisonRelatedFigure() }

    override fun create(command: CreateComparisonRelatedFigureCommand): ThingId {
        val steps = listOf(
            ComparisonRelatedFigureValidator(resourceRepository),
            ComparisonRelatedFigureCreator(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases),
        )
        return steps.execute(command, CreateComparisonRelatedFigureState()).comparisonRelatedFigureId!!
    }

    override fun update(command: UpdateComparisonRelatedFigureCommand) {
        ComparisonRelatedFigureUpdater(
            comparisonRelatedFigureUseCases = this,
            resourceService = resourceService,
            unsafeLiteralUseCases = unsafeLiteralUseCases,
            statementService = statementService,
            unsafeStatementUseCases = unsafeStatementUseCases,
        ).execute(command)
    }

    override fun deleteByIdAndComparisonId(
        comparisonId: ThingId,
        comparisonRelatedFigureId: ThingId,
        contributorId: ContributorId,
    ) {
        ComparisonRelatedFigureDeleter(statementService, resourceService)
            .execute(comparisonId, comparisonRelatedFigureId, contributorId)
    }

    private fun Resource.toComparisonRelatedFigure(): ComparisonRelatedFigure {
        val statements = statementRepository.findAll(subjectId = id, pageable = PageRequests.ALL)
            .content
            .withoutObjectsWithBlankLabels()
        return ComparisonRelatedFigure(
            id = id,
            label = label,
            image = statements.wherePredicate(Predicates.hasImage).firstObjectLabel(),
            description = statements.wherePredicate(Predicates.description).firstObjectLabel(),
            createdAt = createdAt,
            createdBy = createdBy,
        )
    }
}
