package org.orkg.contenttypes.domain

import dev.forkhandles.values.ofOrNull
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.community.output.ConferenceSeriesRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.CreateComparisonState
import org.orkg.contenttypes.domain.actions.DescriptionValidator
import org.orkg.contenttypes.domain.actions.LabelCollectionValidator
import org.orkg.contenttypes.domain.actions.LabelValidator
import org.orkg.contenttypes.domain.actions.ObservatoryValidator
import org.orkg.contenttypes.domain.actions.OrganizationOrConferenceValidator
import org.orkg.contenttypes.domain.actions.PublishComparisonCommand
import org.orkg.contenttypes.domain.actions.PublishComparisonState
import org.orkg.contenttypes.domain.actions.ResearchFieldValidator
import org.orkg.contenttypes.domain.actions.SDGValidator
import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.UpdateComparisonState
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonAuthorCreateValidator
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonAuthorCreator
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonAuthorUpdateValidator
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonAuthorUpdater
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonContributionCreator
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonContributionUpdater
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonContributionValidator
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonDescriptionCreator
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonDescriptionUpdater
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonExistenceValidator
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonIsAnonymizedCreator
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonIsAnonymizedUpdater
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonModifiableValidator
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonPublicationInfoUpdater
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonPublishableValidator
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonReferencesCreator
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonReferencesUpdater
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonRelatedFigureDeleter
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonRelatedFigureUpdater
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonRelatedResourceDeleter
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonRelatedResourceUpdater
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonResearchFieldCreator
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonResearchFieldUpdater
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonResourceCreator
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonResourceUpdater
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonSDGCreator
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonSDGUpdater
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonVersionArchiver
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonVersionDoiPublisher
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.input.ComparisonUseCases
import org.orkg.contenttypes.input.CreateComparisonUseCase.CreateComparisonRelatedFigureCommand
import org.orkg.contenttypes.input.CreateComparisonUseCase.CreateComparisonRelatedResourceCommand
import org.orkg.contenttypes.input.RetrieveComparisonContributionsUseCase
import org.orkg.contenttypes.input.UpdateComparisonUseCase.UpdateComparisonRelatedFigureCommand
import org.orkg.contenttypes.input.UpdateComparisonUseCase.UpdateComparisonRelatedResourceCommand
import org.orkg.contenttypes.output.ComparisonPublishedRepository
import org.orkg.contenttypes.output.ComparisonRepository
import org.orkg.contenttypes.output.ContributionComparisonRepository
import org.orkg.contenttypes.output.DoiService
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.Label
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ListRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ComparisonService(
    private val repository: ContributionComparisonRepository,
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val observatoryRepository: ObservatoryRepository,
    private val organizationRepository: OrganizationRepository,
    private val resourceService: ResourceUseCases,
    private val statementService: StatementUseCases,
    private val literalService: LiteralUseCases,
    private val listService: ListUseCases,
    private val listRepository: ListRepository,
    private val doiService: DoiService,
    private val conferenceSeriesRepository: ConferenceSeriesRepository,
    private val comparisonRepository: ComparisonRepository,
    private val comparisonPublishedRepository: ComparisonPublishedRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
    @Value("\${orkg.publishing.base-url.comparison}")
    private val comparisonPublishBaseUri: String = "http://localhost/comparison/"
) : ComparisonUseCases, RetrieveComparisonContributionsUseCase {
    override fun findById(id: ThingId): Optional<Comparison> =
        resourceRepository.findById(id)
            .filter { Classes.comparison in it.classes }
            .map { it.toComparison() }

    override fun findAll(
        pageable: Pageable,
        label: SearchString?,
        doi: String?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
        researchField: ThingId?,
        includeSubfields: Boolean,
        sustainableDevelopmentGoal: ThingId?
    ): Page<Comparison> =
        comparisonRepository.findAll(
            pageable = pageable,
            label = label,
            doi = doi,
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            researchField = researchField,
            includeSubfields = includeSubfields,
            sustainableDevelopmentGoal = sustainableDevelopmentGoal
        ).pmap { it.toComparison() }

    override fun findRelatedResourceById(comparisonId: ThingId, id: ThingId): Optional<ComparisonRelatedResource> =
        statementRepository.findAll(
            subjectId = comparisonId,
            predicateId = Predicates.hasRelatedResource,
            objectId = id,
            pageable = PageRequests.SINGLE
        )
            .filter { it.`object` is Resource && Classes.comparisonRelatedResource in (it.`object` as Resource).classes }
            .singleOrNull()
            .let { Optional.ofNullable(it) }
            .map { (it.`object` as Resource).toComparisonRelatedResource() }

    override fun findAllRelatedResources(comparisonId: ThingId, pageable: Pageable): Page<ComparisonRelatedResource> =
        statementRepository.findAll(subjectId = comparisonId, predicateId = Predicates.hasRelatedResource, pageable = pageable)
            .map { (it.`object` as Resource).toComparisonRelatedResource() }

    override fun findRelatedFigureById(comparisonId: ThingId, id: ThingId): Optional<ComparisonRelatedFigure> =
        statementRepository.findAll(
            subjectId = comparisonId,
            predicateId = Predicates.hasRelatedFigure,
            objectId = id,
            pageable = PageRequests.SINGLE
        )
            .filter { it.`object` is Resource && Classes.comparisonRelatedFigure in (it.`object` as Resource).classes }
            .singleOrNull()
            .let { Optional.ofNullable(it) }
            .map { (it.`object` as Resource).toComparisonRelatedFigure() }

    override fun findAllRelatedFigures(comparisonId: ThingId, pageable: Pageable): Page<ComparisonRelatedFigure> =
        statementRepository.findAll(subjectId = comparisonId, predicateId = Predicates.hasRelatedFigure, pageable = pageable)
            .map { (it.`object` as Resource).toComparisonRelatedFigure() }

    override fun findAllCurrentListedAndUnpublishedComparisons(pageable: Pageable): Page<Comparison> =
        comparisonRepository.findAllCurrentListedAndUnpublishedComparisons(pageable)
            .map { it.toComparison() }

    override fun findContributionsDetailsById(ids: List<ThingId>, pageable: Pageable): Page<ContributionInfo> =
        repository.findContributionsDetailsById(ids, pageable)

    override fun create(command: CreateComparisonCommand): ThingId {
        val steps = listOf(
            LabelValidator("title") { it.title },
            DescriptionValidator { it.description },
            LabelCollectionValidator("references") { it.references },
            ComparisonContributionValidator(resourceRepository) { it.contributions },
            ResearchFieldValidator(resourceRepository, { it.researchFields }),
            ObservatoryValidator(observatoryRepository, { it.observatories }),
            OrganizationOrConferenceValidator(organizationRepository, conferenceSeriesRepository, { it.organizations }),
            SDGValidator({ it.sustainableDevelopmentGoals }),
            ComparisonAuthorCreateValidator(resourceRepository, statementRepository),
            ComparisonResourceCreator(resourceService),
            ComparisonDescriptionCreator(literalService, statementService),
            ComparisonAuthorCreator(resourceService, statementService, literalService, listService),
            ComparisonSDGCreator(literalService, statementService),
            ComparisonResearchFieldCreator(literalService, statementService),
            ComparisonReferencesCreator(literalService, statementService),
            ComparisonIsAnonymizedCreator(literalService, statementService),
            ComparisonContributionCreator(statementService)
        )
        return steps.execute(command, CreateComparisonState()).comparisonId!!
    }

    override fun createComparisonRelatedResource(command: CreateComparisonRelatedResourceCommand): ThingId {
        Label.ofOrNull(command.label) ?: throw InvalidLabel()
        resourceRepository.findById(command.comparisonId)
            .filter { Classes.comparison in it.classes }
            .orElseThrow { ComparisonNotFound(command.comparisonId) }
        val resourceId = resourceService.createUnsafe(
            CreateResourceUseCase.CreateCommand(
                contributorId = command.contributorId,
                label = command.label,
                classes = setOf(Classes.comparisonRelatedResource)
            )
        )
        statementService.add(
            userId = command.contributorId,
            subject = command.comparisonId,
            predicate = Predicates.hasRelatedResource,
            `object` = resourceId
        )
        if (command.image != null) {
            statementService.add(
                userId = command.contributorId,
                subject = resourceId,
                predicate = Predicates.hasImage,
                `object` = literalService.create(
                    CreateCommand(
                        contributorId = command.contributorId,
                        label = command.image!!
                    )
                )
            )
        }
        if (command.url != null) {
            statementService.add(
                userId = command.contributorId,
                subject = resourceId,
                predicate = Predicates.hasURL,
                `object` = literalService.create(
                    CreateCommand(
                        contributorId = command.contributorId,
                        label = command.url!!
                    )
                )
            )
        }
        if (command.description != null) {
            statementService.add(
                userId = command.contributorId,
                subject = resourceId,
                predicate = Predicates.description,
                `object` = literalService.create(
                    CreateCommand(
                        contributorId = command.contributorId,
                        label = command.description!!
                    )
                )
            )
        }
        return resourceId
    }

    override fun createComparisonRelatedFigure(command: CreateComparisonRelatedFigureCommand): ThingId {
        Label.ofOrNull(command.label) ?: throw InvalidLabel()
        resourceRepository.findById(command.comparisonId)
            .filter { Classes.comparison in it.classes }
            .orElseThrow { ComparisonNotFound(command.comparisonId) }
        val figureId = resourceService.createUnsafe(
            CreateResourceUseCase.CreateCommand(
                contributorId = command.contributorId,
                label = command.label,
                classes = setOf(Classes.comparisonRelatedFigure)
            )
        )
        statementService.add(
            userId = command.contributorId,
            subject = command.comparisonId,
            predicate = Predicates.hasRelatedFigure,
            `object` = figureId
        )
        if (command.image != null) {
            statementService.add(
                userId = command.contributorId,
                subject = figureId,
                predicate = Predicates.hasImage,
                `object` = literalService.create(
                    CreateCommand(
                        contributorId = command.contributorId,
                        label = command.image!!
                    )
                )
            )
        }
        if (command.description != null) {
            statementService.add(
                userId = command.contributorId,
                subject = figureId,
                predicate = Predicates.description,
                `object` = literalService.create(
                    CreateCommand(
                        contributorId = command.contributorId,
                        label = command.description!!
                    )
                )
            )
        }
        return figureId
    }

    override fun update(command: UpdateComparisonCommand) {
        val steps = listOf(
            LabelValidator("title") { it.title },
            DescriptionValidator { it.description },
            LabelCollectionValidator("references") { it.references },
            ComparisonExistenceValidator(this),
            ComparisonModifiableValidator(statementService),
            ComparisonContributionValidator(resourceRepository) { it.contributions },
            ResearchFieldValidator(resourceRepository, { it.researchFields }),
            ObservatoryValidator(observatoryRepository, { it.observatories }),
            OrganizationOrConferenceValidator(organizationRepository, conferenceSeriesRepository, { it.organizations }),
            SDGValidator({ it.sustainableDevelopmentGoals }),
            ComparisonAuthorUpdateValidator(resourceRepository, statementRepository),
            ComparisonResourceUpdater(resourceService),
            ComparisonDescriptionUpdater(literalService, statementService),
            ComparisonResearchFieldUpdater(literalService, statementService),
            ComparisonAuthorUpdater(resourceService, statementService, literalService, listService, listRepository),
            ComparisonSDGUpdater(literalService, statementService),
            ComparisonContributionUpdater(literalService, statementService),
            ComparisonReferencesUpdater(literalService, statementService),
            ComparisonIsAnonymizedUpdater(literalService, statementService)
        )
        steps.execute(command, UpdateComparisonState())
    }

    override fun updateComparisonRelatedResource(command: UpdateComparisonRelatedResourceCommand) {
        ComparisonRelatedResourceUpdater(
            comparisonService = this,
            resourceService = resourceService,
            literalService = literalService,
            statementService = statementService
        ).execute(command)
    }

    override fun updateComparisonRelatedFigure(command: UpdateComparisonRelatedFigureCommand) {
        ComparisonRelatedFigureUpdater(
            comparisonService = this,
            resourceService = resourceService,
            literalService = literalService,
            statementService = statementService
        ).execute(command)
    }

    override fun deleteComparisonRelatedResource(
        comparisonId: ThingId,
        comparisonRelatedResourceId: ThingId,
        contributorId: ContributorId
    ) {
        ComparisonRelatedResourceDeleter(statementService, resourceService)
            .execute(comparisonId, comparisonRelatedResourceId, contributorId)
    }

    override fun deleteComparisonRelatedFigure(
        comparisonId: ThingId,
        comparisonRelatedFigureId: ThingId,
        contributorId: ContributorId
    ) {
        ComparisonRelatedFigureDeleter(statementService, resourceService)
            .execute(comparisonId, comparisonRelatedFigureId, contributorId)
    }

    override fun publish(command: PublishComparisonCommand) {
        val steps = listOf<Action<PublishComparisonCommand, PublishComparisonState>>(
            ComparisonPublishableValidator(resourceService, comparisonPublishedRepository),
            ComparisonPublicationInfoUpdater(literalService, statementService, clock),
            ComparisonVersionArchiver(comparisonPublishedRepository),
            ComparisonVersionDoiPublisher(statementService, literalService, comparisonRepository, doiService, comparisonPublishBaseUri)
        )
        steps.execute(command, PublishComparisonState())
    }

    private fun Resource.toComparisonRelatedResource(): ComparisonRelatedResource {
        val statements = statementRepository.findAll(subjectId = id, pageable = PageRequests.ALL)
            .content
            .withoutObjectsWithBlankLabels()
        return ComparisonRelatedResource(
            id = this@toComparisonRelatedResource.id,
            label = this@toComparisonRelatedResource.label,
            image = statements.wherePredicate(Predicates.hasImage).firstObjectLabel(),
            url = statements.wherePredicate(Predicates.hasURL).firstObjectLabel(),
            description = statements.wherePredicate(Predicates.description).firstObjectLabel(),
            createdAt = this@toComparisonRelatedResource.createdAt,
            createdBy = this@toComparisonRelatedResource.createdBy
        )
    }

    private fun Resource.toComparisonRelatedFigure(): ComparisonRelatedFigure {
        val statements = statementRepository.findAll(subjectId = id, pageable = PageRequests.ALL)
            .content
            .withoutObjectsWithBlankLabels()
        return ComparisonRelatedFigure(
            id = this@toComparisonRelatedFigure.id,
            label = this@toComparisonRelatedFigure.label,
            image = statements.wherePredicate(Predicates.hasImage).firstObjectLabel(),
            description = statements.wherePredicate(Predicates.description).firstObjectLabel(),
            createdAt = this@toComparisonRelatedFigure.createdAt,
            createdBy = this@toComparisonRelatedFigure.createdBy
        )
    }

    internal fun findSubgraph(resource: Resource): ContentTypeSubgraph {
        val statements = (
            statementRepository.fetchAsBundle(
                id = resource.id,
                configuration = BundleConfiguration(
                    minLevel = null,
                    maxLevel = 3,
                    blacklist = listOf(
                        Classes.researchField,
                        Classes.contribution,
                        Classes.visualization,
                        Classes.comparisonRelatedFigure,
                        Classes.comparisonRelatedResource,
                        Classes.sustainableDevelopmentGoal
                    ),
                    whitelist = emptyList()
                ),
                sort = Sort.unsorted()
            ) + statementRepository.fetchAsBundle(
                id = resource.id,
                configuration = BundleConfiguration(
                    minLevel = null,
                    maxLevel = 1,
                    blacklist = emptyList(),
                    whitelist = listOf(
                        Classes.researchField,
                        Classes.contribution,
                        Classes.visualization,
                        Classes.comparisonRelatedFigure,
                        Classes.comparisonRelatedResource,
                        Classes.sustainableDevelopmentGoal
                    )
                ),
                sort = Sort.unsorted()
            )
        ).groupBy { it.subject.id }
        return ContentTypeSubgraph(resource.id, statements)
    }

    internal fun Resource.toComparison(): Comparison =
        findSubgraph(this).let { Comparison.from(this, it.statements, comparisonRepository.findVersionHistory(id)) }
}
