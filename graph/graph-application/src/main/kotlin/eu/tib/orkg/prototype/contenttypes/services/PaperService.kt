package eu.tib.orkg.prototype.contenttypes.services

import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.community.spi.ObservatoryRepository
import eu.tib.orkg.prototype.contenttypes.api.Identifiers
import eu.tib.orkg.prototype.contenttypes.api.PaperUseCases
import eu.tib.orkg.prototype.contenttypes.application.PaperNotFound
import eu.tib.orkg.prototype.contenttypes.domain.model.Paper
import eu.tib.orkg.prototype.contenttypes.domain.model.PublicationInfo
import eu.tib.orkg.prototype.contenttypes.services.actions.AuthorValidator
import eu.tib.orkg.prototype.contenttypes.services.actions.ContributionValidator
import eu.tib.orkg.prototype.contenttypes.services.actions.ObservatoryValidator
import eu.tib.orkg.prototype.contenttypes.services.actions.OrganizationValidator
import eu.tib.orkg.prototype.contenttypes.services.actions.PaperAction
import eu.tib.orkg.prototype.contenttypes.services.actions.PaperAuthorCreator
import eu.tib.orkg.prototype.contenttypes.services.actions.PaperContentsCreator
import eu.tib.orkg.prototype.contenttypes.services.actions.PaperExistenceValidator
import eu.tib.orkg.prototype.contenttypes.services.actions.PaperPublicationInfoCreator
import eu.tib.orkg.prototype.contenttypes.services.actions.PaperResearchFieldCreator
import eu.tib.orkg.prototype.contenttypes.services.actions.PaperResourceCreator
import eu.tib.orkg.prototype.contenttypes.services.actions.ResearchFieldValidator
import eu.tib.orkg.prototype.contenttypes.services.actions.TempIdValidator
import eu.tib.orkg.prototype.contenttypes.services.actions.ThingDefinitionValidator
import eu.tib.orkg.prototype.shared.PageRequests
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.ListUseCases
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.PredicateUseCases
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.api.VisibilityFilter
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.spi.ThingRepository
import java.net.URI
import java.util.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import eu.tib.orkg.prototype.contenttypes.api.CreateContributionUseCase.CreateCommand as CreateContributionCommand
import eu.tib.orkg.prototype.contenttypes.api.CreatePaperUseCase.CreateCommand as CreatePaperCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.ContributionAction.State as ContributionState

@Service
class PaperService(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val observatoryRepository: ObservatoryRepository,
    private val organizationRepository: PostgresOrganizationRepository,
    private val thingRepository: ThingRepository,
    private val resourceService: ResourceUseCases,
    private val statementService: StatementUseCases,
    private val literalService: LiteralUseCases,
    private val predicateService: PredicateUseCases,
    private val listService: ListUseCases,
    private val publishingService: PublishingService,
    @Value("\${orkg.publishing.base-url.paper}")
    private val paperPublishBaseUri: String = "http://localhost/paper/"
) : PaperUseCases {
    override fun findById(id: ThingId): Optional<Paper> =
        resourceRepository.findPaperById(id)
            .map { it.toPaper() }

    override fun findAll(pageable: Pageable): Page<Paper> =
        resourceRepository.findAllByClass(Classes.paper, pageable)
            .pmap { it.toPaper() }

    override fun findAllByDOI(doi: String, pageable: Pageable): Page<Paper> =
        statementRepository.findAllBySubjectClassAndDOI(Classes.paper, doi, pageable)
            .pmap { it.toPaper() }

    override fun findAllByTitle(title: String, pageable: Pageable): Page<Paper> =
        resourceRepository.findAllByClassAndLabel(Classes.paper, SearchString.of(title, exactMatch = true), pageable)
            .pmap { it.toPaper() }

    override fun findAllByVisibility(visibility: VisibilityFilter, pageable: Pageable): Page<Paper> =
        when (visibility) {
            VisibilityFilter.ALL_LISTED -> resourceRepository.findAllListedByClass(Classes.paper, pageable)
            VisibilityFilter.NON_FEATURED -> resourceRepository.findAllByClassAndVisibility(Classes.paper, Visibility.DEFAULT, pageable)
            VisibilityFilter.UNLISTED -> resourceRepository.findAllByClassAndVisibility(Classes.paper, Visibility.UNLISTED, pageable)
            VisibilityFilter.FEATURED -> resourceRepository.findAllByClassAndVisibility(Classes.paper, Visibility.FEATURED, pageable)
            VisibilityFilter.DELETED -> resourceRepository.findAllByClassAndVisibility(Classes.paper, Visibility.DELETED, pageable)
        }.pmap { it.toPaper() }

    override fun findAllByContributor(contributorId: ContributorId, pageable: Pageable): Page<Paper> =
        resourceRepository.findAllByClassAndCreatedBy(Classes.paper, contributorId, pageable)
            .pmap { it.toPaper() }

    override fun findAllContributorsByPaperId(id: ThingId, pageable: Pageable): Page<ContributorId> =
        resourceRepository.findPaperById(id)
            .map { statementRepository.findAllContributorsByResourceId(id, pageable) }
            .orElseThrow { PaperNotFound(id) }

    override fun create(command: CreatePaperCommand): ThingId {
        val steps = listOf(
            TempIdValidator(),
            PaperExistenceValidator(resourceRepository, resourceService, statementRepository),
            ResearchFieldValidator(resourceRepository),
            ObservatoryValidator(observatoryRepository),
            OrganizationValidator(organizationRepository),
            AuthorValidator(resourceRepository, statementRepository),
            ThingDefinitionValidator(thingRepository),
            ContributionValidator(thingRepository),
            PaperResourceCreator(resourceService),
            PaperAuthorCreator(resourceService, statementService, literalService, listService),
            PaperResearchFieldCreator(statementService),
            PaperPublicationInfoCreator(resourceService, resourceRepository, statementService, literalService),
            PaperContentsCreator(resourceService, statementService, literalService, predicateService, statementRepository, listService)
        )
        return steps.fold(PaperAction.State()) { state, executor -> executor(command, state) }.paperId!!
    }

    override fun createContribution(command: CreateContributionCommand): ThingId {
        val steps = listOf(
            TempIdValidator(),
            PaperExistenceValidator(resourceRepository, resourceService, statementRepository),
            ThingDefinitionValidator(thingRepository),
            ContributionValidator(thingRepository),
            PaperContentsCreator(resourceService, statementService, literalService, predicateService, statementRepository, listService)
        )
        return steps.fold(ContributionState()) { state, executor -> executor(command, state) }.contributionId!!
    }

    override fun publish(id: ThingId, subject: String, description: String) {
        val paper = findById(id).orElseThrow { PaperNotFound(id) }
        publishingService.publish(
            PublishingService.PublishCommand(
                id = id,
                title = paper.title,
                subject = subject,
                description = description,
                url = URI.create("$paperPublishBaseUri/").resolve(id.value),
                creators = paper.authors,
                resourceType = Classes.paper,
                relatedIdentifiers = emptyList()
            )
        )
    }

    private fun Resource.toPaper(): Paper {
        val statements = statementRepository.findAllBySubject(id, PageRequests.ALL)
            .content
            .withoutObjectsWithBlankLabels()
        return Paper(
            id = id,
            title = label,
            researchFields = statements.wherePredicate(Predicates.hasResearchField)
                .objectIdsAndLabel()
                .sortedBy { it.id },
            identifiers = statements.associateIdentifiers(Identifiers.paper),
            publicationInfo = PublicationInfo.from(statements),
            authors = statements.authors(statementRepository),
            contributions = statements.wherePredicate(Predicates.hasContribution).objectIdsAndLabel(),
            observatories = listOf(observatoryId),
            organizations = listOf(organizationId),
            extractionMethod = extractionMethod,
            createdAt = createdAt,
            createdBy = createdBy,
            verified = verified ?: false,
            visibility = visibility
        )
    }
}
