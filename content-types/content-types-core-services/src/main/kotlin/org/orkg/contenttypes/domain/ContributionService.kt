package org.orkg.contenttypes.domain

import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.pmap
import org.orkg.contenttypes.domain.actions.ContributionState
import org.orkg.contenttypes.domain.actions.CreateContributionCommand
import org.orkg.contenttypes.domain.actions.TempIdValidator
import org.orkg.contenttypes.domain.actions.contributions.ContributionContentsCreator
import org.orkg.contenttypes.domain.actions.contributions.ContributionContentsValidator
import org.orkg.contenttypes.domain.actions.contributions.ContributionPaperValidator
import org.orkg.contenttypes.domain.actions.contributions.ContributionThingsCommandValidator
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.input.ContributionUseCases
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Resource
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeClassUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class ContributionService(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val thingRepository: ThingRepository,
    private val unsafeClassUseCases: UnsafeClassUseCases,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
    private val unsafePredicateUseCases: UnsafePredicateUseCases,
    private val listService: ListUseCases,
    private val classRepository: ClassRepository,
) : ContributionUseCases {
    override fun findById(id: ThingId): Optional<Contribution> =
        resourceRepository.findById(id)
            .filter { Classes.contribution in it.classes }
            .map { it.toContribution() }

    override fun findAll(pageable: Pageable): Page<Contribution> =
        resourceRepository.findAll(includeClasses = setOf(Classes.contribution), pageable = pageable)
            .pmap { it.toContribution() }

    override fun create(command: CreateContributionCommand): ThingId {
        val steps = listOf(
            TempIdValidator { it.tempIds() },
            ContributionPaperValidator(resourceRepository),
            ContributionThingsCommandValidator(thingRepository, classRepository),
            ContributionContentsValidator(thingRepository),
            ContributionContentsCreator(unsafeClassUseCases, unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases, unsafePredicateUseCases, statementRepository, listService)
        )
        return steps.execute(command, ContributionState()).contributionId!!
    }

    private fun Resource.toContribution(): Contribution {
        val statements = statementRepository.findAll(subjectId = id, pageable = PageRequests.ALL).content
            .withoutObjectsWithBlankLabels()
        return Contribution(
            id = this@toContribution.id,
            label = this@toContribution.label,
            classes = this@toContribution.classes,
            properties = statements.toContributionSubGraph(),
            extractionMethod = extractionMethod,
            createdAt = createdAt,
            createdBy = createdBy,
            visibility = this@toContribution.visibility,
            unlistedBy = unlistedBy
        )
    }
}
