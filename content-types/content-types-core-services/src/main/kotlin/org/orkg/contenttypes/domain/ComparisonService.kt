package org.orkg.contenttypes.domain

import java.net.URI
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.ObservatoryValidator
import org.orkg.contenttypes.domain.actions.OrganizationValidator
import org.orkg.contenttypes.domain.actions.ResearchFieldValidator
import org.orkg.contenttypes.domain.actions.comparison.ComparisonAction
import org.orkg.contenttypes.domain.actions.comparison.ComparisonAuthorCreator
import org.orkg.contenttypes.domain.actions.comparison.ComparisonAuthorValidator
import org.orkg.contenttypes.domain.actions.comparison.ComparisonContributionCreator
import org.orkg.contenttypes.domain.actions.comparison.ComparisonContributionValidator
import org.orkg.contenttypes.domain.actions.comparison.ComparisonDescriptionCreator
import org.orkg.contenttypes.domain.actions.comparison.ComparisonResearchFieldCreator
import org.orkg.contenttypes.domain.actions.comparison.ComparisonResourceCreator
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.input.ComparisonUseCases
import org.orkg.contenttypes.input.CreateComparisonUseCase.CreateComparisonRelatedFigureCommand
import org.orkg.contenttypes.input.CreateComparisonUseCase.CreateComparisonRelatedResourceCommand
import org.orkg.contenttypes.input.PublishComparisonUseCase
import org.orkg.contenttypes.input.RetrieveComparisonContributionsUseCase
import org.orkg.contenttypes.input.RetrieveResearchFieldUseCase
import org.orkg.contenttypes.output.ContributionComparisonRepository
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ContributionInfo
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.authors
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
    private val researchFieldService: RetrieveResearchFieldUseCase,
    private val publishingService: PublishingService,
    @Value("\${orkg.publishing.base-url.comparison}")
    private val comparisonPublishBaseUri: String = "http://localhost/comparison/"
) : ComparisonUseCases, RetrieveComparisonContributionsUseCase {
    override fun findById(id: ThingId): Optional<Comparison> =
        resourceRepository.findById(id)
            .filter { Classes.comparison in it.classes }
            .map { it.toComparison() }

    override fun findAll(pageable: Pageable): Page<Comparison> =
        statementRepository.findAllCurrentComparisons(pageable)
            .pmap { it.toComparison() }

    override fun findAllByDOI(doi: String, pageable: Pageable): Page<Comparison> =
        statementRepository.findAllBySubjectClassAndDOI(Classes.comparison, doi, pageable)
            .pmap { it.toComparison() }

    override fun findAllByTitle(title: String, pageable: Pageable): Page<Comparison> =
        resourceRepository.findAllByClassAndLabel(Classes.comparison, SearchString.of(title, exactMatch = true), pageable)
            .pmap { it.toComparison() }

    override fun findAllByVisibility(visibility: VisibilityFilter, pageable: Pageable): Page<Comparison> =
        when (visibility) {
            VisibilityFilter.ALL_LISTED -> statementRepository.findAllCurrentListedComparisons(pageable)
            VisibilityFilter.NON_FEATURED -> statementRepository.findAllCurrentComparisonsByVisibility(Visibility.DEFAULT, pageable)
            VisibilityFilter.UNLISTED -> statementRepository.findAllCurrentComparisonsByVisibility(Visibility.UNLISTED, pageable)
            VisibilityFilter.FEATURED -> statementRepository.findAllCurrentComparisonsByVisibility(Visibility.FEATURED, pageable)
            VisibilityFilter.DELETED -> statementRepository.findAllCurrentComparisonsByVisibility(Visibility.DELETED, pageable)
        }.pmap { it.toComparison() }

    override fun findRelatedResourceById(comparisonId: ThingId, id: ThingId): Optional<ComparisonRelatedResource> =
        statementRepository.findBySubjectIdAndPredicateIdAndObjectId(comparisonId, Predicates.hasRelatedFigure, id)
            .filter { it.`object` is Resource && Classes.comparisonRelatedResource in (it.`object` as Resource).classes }
            .map { (it.`object` as Resource).toComparisonRelatedResource() }

    override fun findAllRelatedResources(comparisonId: ThingId, pageable: Pageable): Page<ComparisonRelatedResource> =
        statementRepository.findAllBySubjectAndPredicate(comparisonId, Predicates.hasRelatedFigure, pageable)
            .map { (it.`object` as Resource).toComparisonRelatedResource() }

    override fun findRelatedFigureById(comparisonId: ThingId, id: ThingId): Optional<ComparisonRelatedFigure> =
        statementRepository.findBySubjectIdAndPredicateIdAndObjectId(comparisonId, Predicates.hasRelatedFigure, id)
            .filter { it.`object` is Resource && Classes.comparisonRelatedFigure in (it.`object` as Resource).classes }
            .map { (it.`object` as Resource).toComparisonRelatedFigure() }

    override fun findAllRelatedFigures(comparisonId: ThingId, pageable: Pageable): Page<ComparisonRelatedFigure> =
        statementRepository.findAllBySubjectAndPredicate(comparisonId, Predicates.hasRelatedFigure, pageable)
            .map { (it.`object` as Resource).toComparisonRelatedFigure() }

    override fun findAllCurrentListedAndUnpublishedComparisons(pageable: Pageable): Page<Comparison> =
        statementRepository.findAllCurrentListedAndUnpublishedComparisons(pageable)
            .map { it.toComparison() }

    override fun findAllByResearchFieldAndVisibility(
        researchFieldId: ThingId,
        visibility: VisibilityFilter,
        includeSubfields: Boolean,
        pageable: Pageable
    ): Page<Comparison> =
        researchFieldService.findAllComparisonsByResearchField(researchFieldId, visibility, includeSubfields, pageable)
            .pmap { it.toComparison() }

    override fun findAllByContributor(contributorId: ContributorId, pageable: Pageable): Page<Comparison> =
        resourceRepository.findAllByClassAndCreatedBy(Classes.comparison, contributorId, pageable)
            .pmap { it.toComparison() }

    override fun findContributionsDetailsById(ids: List<ThingId>, pageable: Pageable): Page<ContributionInfo> =
        repository.findContributionsDetailsById(ids, pageable)

    override fun create(command: CreateComparisonCommand): ThingId {
        val steps = listOf(
            ComparisonContributionValidator(resourceRepository),
            ResearchFieldValidator(resourceRepository) { it.researchFields },
            ObservatoryValidator(observatoryRepository) { it.observatories },
            OrganizationValidator(organizationRepository) { it.organizations },
            ComparisonAuthorValidator(resourceRepository, statementRepository),
            ComparisonResourceCreator(resourceService),
            ComparisonDescriptionCreator(literalService, statementService),
            ComparisonAuthorCreator(resourceService, statementService, literalService, listService),
            ComparisonResearchFieldCreator(statementService),
            ComparisonContributionCreator(statementService)
        )
        return steps.execute(command, ComparisonAction.State()).comparisonId!!
    }

    override fun createComparisonRelatedResource(command: CreateComparisonRelatedResourceCommand): ThingId {
        resourceRepository.findById(command.comparisonId)
            .filter { Classes.comparison in it.classes }
            .orElseThrow { ComparisonNotFound(command.comparisonId) }
        val resourceId = resourceService.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = command.contributorId,
                label = command.label,
                classes = setOf(Classes.comparisonRelatedResource)
            )
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
        resourceRepository.findById(command.comparisonId)
            .filter { Classes.comparison in it.classes }
            .orElseThrow { ComparisonNotFound(command.comparisonId) }
        val figureId = resourceService.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = command.contributorId,
                label = command.label,
                classes = setOf(Classes.comparisonRelatedFigure)
            )
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

    override fun publish(command: PublishComparisonUseCase.PublishCommand) {
        val comparison = resourceRepository.findById(command.id)
            .filter { Classes.comparison in it.classes }
            .orElseThrow { ComparisonNotFound(command.id) }
        publishingService.publish(
            PublishingService.PublishCommand(
                id = comparison.id,
                title = comparison.label,
                contributorId = command.contributorId,
                subject = command.subject,
                description = command.description,
                url = URI.create("$comparisonPublishBaseUri/").resolve(comparison.id.value),
                creators = command.authors,
                resourceType = Classes.comparison,
                relatedIdentifiers = statementRepository.findAllDOIsRelatedToComparison(comparison.id).toList()
            )
        )
    }

    private fun Resource.toComparisonRelatedResource(): ComparisonRelatedResource {
        val statements = statementRepository.findAllBySubject(id, PageRequests.ALL)
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
        val statements = statementRepository.findAllBySubject(id, PageRequests.ALL)
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

    private fun Resource.toComparison(): Comparison {
        val statements = statementRepository.fetchAsBundle(
            id = id,
            configuration = BundleConfiguration(
                minLevel = null,
                maxLevel = 3,
                blacklist = listOf(
                    Classes.researchField,
                    Classes.contribution,
                    Classes.visualization,
                    Classes.comparisonRelatedFigure,
                    Classes.comparisonRelatedResource
                ),
                whitelist = emptyList()
            ),
            sort = Sort.unsorted()
        ).groupBy { it.subject.id }
        val directStatements = statements[id].orEmpty()
        return Comparison(
            id = id,
            title = label,
            description = directStatements.wherePredicate(Predicates.description).firstObjectLabel(),
            researchFields = directStatements.wherePredicate(Predicates.hasSubject).objectIdsAndLabel(),
            identifiers = directStatements.associateIdentifiers(Identifiers.comparison),
            publicationInfo = PublicationInfo.from(directStatements),
            authors = statements.authors(id),
            contributions = directStatements.wherePredicate(Predicates.comparesContribution).objectIdsAndLabel(),
            visualizations = directStatements.wherePredicate(Predicates.hasVisualization).objectIdsAndLabel(),
            relatedFigures = directStatements.wherePredicate(Predicates.hasRelatedFigure).objectIdsAndLabel(),
            relatedResources = directStatements.wherePredicate(Predicates.hasRelatedResource).objectIdsAndLabel(),
            references = directStatements.wherePredicate(Predicates.reference)
                .withoutObjectsWithBlankLabels()
                .objects()
                .filterIsInstance<Literal>()
                .map { it.label },
            observatories = listOf(observatoryId),
            organizations = listOf(organizationId),
            extractionMethod = extractionMethod,
            createdAt = createdAt,
            createdBy = createdBy,
            previousVersion = directStatements.wherePredicate(Predicates.hasPreviousVersion).firstObjectId(),
            isAnonymized = directStatements.wherePredicate(Predicates.isAnonymized)
                .firstOrNull { it.`object` is Literal && (it.`object` as Literal).datatype == Literals.XSD.BOOLEAN.prefixedUri }
                ?.`object`?.label.toBoolean(),
            visibility = visibility,
            unlistedBy = unlistedBy
        )
    }
}
