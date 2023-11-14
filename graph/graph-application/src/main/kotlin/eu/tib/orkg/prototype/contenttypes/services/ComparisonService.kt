package eu.tib.orkg.prototype.contenttypes.services

import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.community.spi.ObservatoryRepository
import eu.tib.orkg.prototype.contenttypes.api.ComparisonUseCases
import eu.tib.orkg.prototype.contenttypes.api.CreateComparisonUseCase.CreateComparisonRelatedFigureCommand
import eu.tib.orkg.prototype.contenttypes.api.CreateComparisonUseCase.CreateComparisonRelatedResourceCommand
import eu.tib.orkg.prototype.contenttypes.api.Identifiers
import eu.tib.orkg.prototype.contenttypes.application.ComparisonNotFound
import eu.tib.orkg.prototype.contenttypes.domain.model.Comparison
import eu.tib.orkg.prototype.contenttypes.domain.model.ComparisonRelatedFigure
import eu.tib.orkg.prototype.contenttypes.domain.model.ComparisonRelatedResource
import eu.tib.orkg.prototype.contenttypes.domain.model.PublicationInfo
import eu.tib.orkg.prototype.contenttypes.services.actions.CreateComparisonCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.comparison.ComparisonAction
import eu.tib.orkg.prototype.contenttypes.services.actions.comparison.ComparisonAuthorCreator
import eu.tib.orkg.prototype.contenttypes.services.actions.comparison.ComparisonAuthorValidator
import eu.tib.orkg.prototype.contenttypes.services.actions.comparison.ComparisonContributionCreator
import eu.tib.orkg.prototype.contenttypes.services.actions.comparison.ComparisonContributionValidator
import eu.tib.orkg.prototype.contenttypes.services.actions.comparison.ComparisonDescriptionCreator
import eu.tib.orkg.prototype.contenttypes.services.actions.comparison.ComparisonObservatoryValidator
import eu.tib.orkg.prototype.contenttypes.services.actions.comparison.ComparisonOrganizationValidator
import eu.tib.orkg.prototype.contenttypes.services.actions.comparison.ComparisonResearchFieldCreator
import eu.tib.orkg.prototype.contenttypes.services.actions.comparison.ComparisonResearchFieldValidator
import eu.tib.orkg.prototype.contenttypes.services.actions.comparison.ComparisonResourceCreator
import eu.tib.orkg.prototype.contenttypes.services.actions.execute
import eu.tib.orkg.prototype.shared.PageRequests
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.CreateResourceUseCase
import eu.tib.orkg.prototype.statements.api.ListUseCases
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.Literals
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldUseCase
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.api.VisibilityFilter
import eu.tib.orkg.prototype.statements.domain.model.ContributionInfo
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.statements.spi.ContributionComparisonRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.net.URI
import java.util.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ComparisonService(
    private val repository: ContributionComparisonRepository,
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val observatoryRepository: ObservatoryRepository,
    private val organizationRepository: PostgresOrganizationRepository,
    private val resourceService: ResourceUseCases,
    private val statementService: StatementUseCases,
    private val literalService: LiteralUseCases,
    private val listService: ListUseCases,
    private val researchFieldService: RetrieveResearchFieldUseCase,
    private val publishingService: PublishingService,
    @Value("\${orkg.publishing.base-url.comparison}")
    private val comparisonPublishBaseUri: String = "http://localhost/comparison/"
) : ComparisonUseCases {
    override fun findById(id: ThingId): Optional<Comparison> =
        resourceRepository.findById(id)
            .filter { it is Resource && Classes.comparison in it.classes }
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
            .filter { it.`object` is Resource && Classes.comparisonRelatedResource in it.`object`.classes }
            .map { it.`object`.toComparisonRelatedResource() }

    override fun findAllRelatedResources(comparisonId: ThingId, pageable: Pageable): Page<ComparisonRelatedResource> =
        statementRepository.findAllBySubjectAndPredicate(comparisonId, Predicates.hasRelatedFigure, pageable)
            .map { it.`object`.toComparisonRelatedResource() }

    override fun findRelatedFigureById(comparisonId: ThingId, id: ThingId): Optional<ComparisonRelatedFigure> =
        statementRepository.findBySubjectIdAndPredicateIdAndObjectId(comparisonId, Predicates.hasRelatedFigure, id)
            .filter { it.`object` is Resource && Classes.comparisonRelatedFigure in it.`object`.classes }
            .map { it.`object`.toComparisonRelatedFigure() }

    override fun findAllRelatedFigures(comparisonId: ThingId, pageable: Pageable): Page<ComparisonRelatedFigure> =
        statementRepository.findAllBySubjectAndPredicate(comparisonId, Predicates.hasRelatedFigure, pageable)
            .map { it.`object`.toComparisonRelatedFigure() }

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
            ComparisonResearchFieldValidator(resourceRepository),
            ComparisonObservatoryValidator(observatoryRepository),
            ComparisonOrganizationValidator(organizationRepository),
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
                `object` = literalService.create(command.contributorId, command.image).id
            )
        }
        if (command.url != null) {
            statementService.add(
                userId = command.contributorId,
                subject = resourceId,
                predicate = Predicates.hasURL,
                `object` = literalService.create(command.contributorId, command.url).id
            )
        }
        if (command.description != null) {
            statementService.add(
                userId = command.contributorId,
                subject = resourceId,
                predicate = Predicates.description,
                `object` = literalService.create(command.contributorId, command.description).id
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
                `object` = literalService.create(command.contributorId, command.image).id
            )
        }
        if (command.description != null) {
            statementService.add(
                userId = command.contributorId,
                subject = figureId,
                predicate = Predicates.description,
                `object` = literalService.create(command.contributorId, command.description).id
            )
        }
        return figureId
    }

    override fun publish(id: ThingId, subject: String, description: String) {
        val comparison = findById(id).orElseThrow { ComparisonNotFound(id) }
        publishingService.publish(
            PublishingService.PublishCommand(
                id = id,
                title = comparison.title,
                subject = subject,
                description = description,
                url = URI.create("$comparisonPublishBaseUri/").resolve(id.value),
                creators = comparison.authors,
                resourceType = Classes.comparison,
                relatedIdentifiers = statementRepository.findAllDOIsRelatedToComparison(id).toList()
            )
        )
    }

    private fun Thing.toComparisonRelatedResource(): ComparisonRelatedResource {
        val statements = statementRepository.findAllBySubject(id, PageRequests.ALL)
            .content
            .withoutObjectsWithBlankLabels()
        return ComparisonRelatedResource(
            id = this@toComparisonRelatedResource.id,
            label = this@toComparisonRelatedResource.label,
            image = statements.wherePredicate(Predicates.hasImage).firstObjectLabel(),
            url = statements.wherePredicate(Predicates.hasURL).firstObjectLabel(),
            description = statements.wherePredicate(Predicates.description).firstObjectLabel()
        )
    }

    private fun Thing.toComparisonRelatedFigure(): ComparisonRelatedFigure {
        val statements = statementRepository.findAllBySubject(id, PageRequests.ALL)
            .content
            .withoutObjectsWithBlankLabels()
        return ComparisonRelatedFigure(
            id = this@toComparisonRelatedFigure.id,
            label = this@toComparisonRelatedFigure.label,
            image = statements.wherePredicate(Predicates.hasImage).firstObjectLabel(),
            description = statements.wherePredicate(Predicates.description).firstObjectLabel()
        )
    }

    private fun Resource.toComparison(): Comparison {
        val statements = statementRepository.findAllBySubject(id, PageRequests.ALL)
            .content
            .withoutObjectsWithBlankLabels()
        return Comparison(
            id = id,
            title = label,
            description = statements.wherePredicate(Predicates.description).firstObjectLabel(),
            researchFields = statements.wherePredicate(Predicates.hasSubject).objectIdsAndLabel(),
            identifiers = statements.associateIdentifiers(Identifiers.comparison),
            publicationInfo = PublicationInfo.from(statements),
            authors = statements.authors(statementRepository),
            contributions = statements.wherePredicate(Predicates.comparesContribution).objectIdsAndLabel(),
            visualizations = statements.wherePredicate(Predicates.hasVisualization).objectIdsAndLabel(),
            relatedFigures = statements.wherePredicate(Predicates.hasRelatedFigure).objectIdsAndLabel(),
            relatedResources = statements.wherePredicate(Predicates.hasRelatedResource).objectIdsAndLabel(),
            references = statements.wherePredicate(Predicates.reference)
                .withoutObjectsWithBlankLabels()
                .objects()
                .filterIsInstance<Literal>()
                .map { it.label },
            observatories = listOf(observatoryId),
            organizations = listOf(organizationId),
            extractionMethod = extractionMethod,
            createdAt = createdAt,
            createdBy = createdBy,
            previousVersion = statements.wherePredicate(Predicates.hasPreviousVersion).firstObjectId(),
            isAnonymized = statements.wherePredicate(Predicates.isAnonymized)
                .firstOrNull { it.`object` is Literal && it.`object`.datatype == Literals.XSD.BOOLEAN.prefixedUri }
                ?.`object`?.label.toBoolean(),
            visibility = visibility
        )
    }
}
