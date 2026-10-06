package org.orkg.contenttypes.domain

import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.CreateComparisonRelatedResourceCommand
import org.orkg.contenttypes.domain.actions.CreateComparisonRelatedResourceState
import org.orkg.contenttypes.domain.actions.UpdateComparisonRelatedResourceCommand
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonRelatedResourceDeleter
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonRelatedResourceUpdater
import org.orkg.contenttypes.domain.actions.comparisons.resources.ComparisonRelatedResourceCreator
import org.orkg.contenttypes.domain.actions.comparisons.resources.ComparisonRelatedResourceValidator
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.input.ComparisonRelatedResourceUseCases
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
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
class ComparisonRelatedResourceService(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val resourceService: ResourceUseCases,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val statementService: StatementUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
) : ComparisonRelatedResourceUseCases {
    override fun findByIdAndComparisonId(comparisonId: ThingId, id: ThingId): Optional<ComparisonRelatedResource> =
        statementRepository.findAll(
            subjectId = comparisonId,
            predicateId = Predicates.hasRelatedResource,
            objectId = id,
            pageable = PageRequests.SINGLE,
        )
            .filter { it.`object` is Resource && Classes.comparisonRelatedResource in (it.`object` as Resource).classes }
            .singleOrNull()
            .let { Optional.ofNullable(it) }
            .map { (it.`object` as Resource).toComparisonRelatedResource() }

    override fun findAllByComparisonId(comparisonId: ThingId, pageable: Pageable): Page<ComparisonRelatedResource> =
        statementRepository.findAll(subjectId = comparisonId, predicateId = Predicates.hasRelatedResource, pageable = pageable)
            .map { (it.`object` as Resource).toComparisonRelatedResource() }

    override fun create(command: CreateComparisonRelatedResourceCommand): ThingId {
        val steps = listOf(
            ComparisonRelatedResourceValidator(resourceRepository),
            ComparisonRelatedResourceCreator(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases),
        )
        return steps.execute(command, CreateComparisonRelatedResourceState()).comparisonRelatedResourceId!!
    }

    override fun update(command: UpdateComparisonRelatedResourceCommand) {
        ComparisonRelatedResourceUpdater(
            comparisonRelatedResourceUseCases = this,
            resourceService = resourceService,
            unsafeLiteralUseCases = unsafeLiteralUseCases,
            statementService = statementService,
            unsafeStatementUseCases = unsafeStatementUseCases,
        ).execute(command)
    }

    override fun deleteByIdAndComparisonId(
        comparisonId: ThingId,
        comparisonRelatedResourceId: ThingId,
        contributorId: ContributorId,
    ) {
        ComparisonRelatedResourceDeleter(statementService, resourceService)
            .execute(comparisonId, comparisonRelatedResourceId, contributorId)
    }

    private fun Resource.toComparisonRelatedResource(): ComparisonRelatedResource {
        val statements = statementRepository.findAll(subjectId = id, pageable = PageRequests.ALL)
            .content
            .withoutObjectsWithBlankLabels()
        return ComparisonRelatedResource(
            id = id,
            label = label,
            image = statements.wherePredicate(Predicates.hasImage).firstObjectLabel(),
            url = statements.wherePredicate(Predicates.hasURL).firstObjectLabel(),
            description = statements.wherePredicate(Predicates.description).firstObjectLabel(),
            createdAt = createdAt,
            createdBy = createdBy,
        )
    }
}
