package org.orkg.contenttypes.domain

import java.net.URI
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.domain.actions.ObservatoryValidator
import org.orkg.contenttypes.domain.actions.OrganizationValidator
import org.orkg.contenttypes.domain.actions.ResearchFieldValidator
import org.orkg.contenttypes.domain.actions.contribution.ContributionContentsCreator
import org.orkg.contenttypes.domain.actions.contribution.ContributionContentsValidator
import org.orkg.contenttypes.domain.actions.contribution.ContributionPaperExistenceValidator
import org.orkg.contenttypes.domain.actions.contribution.ContributionTempIdValidator
import org.orkg.contenttypes.domain.actions.contribution.ContributionThingDefinitionValidator
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.actions.paper.PaperAuthorCreateValidator
import org.orkg.contenttypes.domain.actions.paper.PaperAuthorCreator
import org.orkg.contenttypes.domain.actions.paper.PaperContributionCreator
import org.orkg.contenttypes.domain.actions.paper.PaperContributionValidator
import org.orkg.contenttypes.domain.actions.paper.PaperIdentifierCreateValidator
import org.orkg.contenttypes.domain.actions.paper.PaperIdentifierCreator
import org.orkg.contenttypes.domain.actions.paper.PaperPublicationInfoCreator
import org.orkg.contenttypes.domain.actions.paper.PaperResearchFieldCreator
import org.orkg.contenttypes.domain.actions.paper.PaperResourceCreator
import org.orkg.contenttypes.domain.actions.paper.PaperTempIdValidator
import org.orkg.contenttypes.domain.actions.paper.PaperThingDefinitionValidator
import org.orkg.contenttypes.domain.actions.paper.PaperTitleValidator
import org.orkg.contenttypes.input.PaperUseCases
import org.orkg.contenttypes.input.RetrieveResearchFieldUseCase
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.output.authors
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.orkg.contenttypes.domain.actions.contribution.ContributionAction.State as ContributionState
import org.orkg.contenttypes.input.CreateContributionUseCase.CreateCommand as CreateContributionCommand
import org.orkg.contenttypes.input.CreatePaperUseCase.CreateCommand as CreatePaperCommand

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
    private val researchFieldService: RetrieveResearchFieldUseCase,
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

    override fun findAllByResearchFieldAndVisibility(
        researchFieldId: ThingId,
        visibility: VisibilityFilter,
        includeSubfields: Boolean,
        pageable: Pageable
    ): Page<Paper> =
        researchFieldService.findAllPapersByResearchField(researchFieldId, visibility, includeSubfields, pageable)
            .pmap { it.toPaper() }

    override fun findAllByContributor(contributorId: ContributorId, pageable: Pageable): Page<Paper> =
        resourceRepository.findAllByClassAndCreatedBy(Classes.paper, contributorId, pageable)
            .pmap { it.toPaper() }

    override fun findAllContributorsByPaperId(id: ThingId, pageable: Pageable): Page<ContributorId> =
        resourceRepository.findPaperById(id)
            .map { statementRepository.findAllContributorsByResourceId(id, pageable) }
            .orElseThrow { PaperNotFound(id) }

    override fun create(command: CreatePaperCommand): ThingId {
        val steps = listOf(
            PaperTempIdValidator(),
            PaperTitleValidator(resourceService),
            PaperIdentifierCreateValidator(statementRepository),
            ResearchFieldValidator(resourceRepository) { it.researchFields },
            ObservatoryValidator(observatoryRepository) { it.observatories },
            OrganizationValidator(organizationRepository) { it.organizations },
            PaperAuthorCreateValidator(resourceRepository, statementRepository),
            PaperThingDefinitionValidator(thingRepository),
            PaperContributionValidator(thingRepository),
            PaperResourceCreator(resourceService),
            PaperIdentifierCreator(statementService, literalService),
            PaperAuthorCreator(resourceService, statementService, literalService, listService),
            PaperResearchFieldCreator(statementService),
            PaperPublicationInfoCreator(resourceService, resourceRepository, statementService, literalService),
            PaperContributionCreator(resourceService, statementService, literalService, predicateService, statementRepository, listService)
        )
        return steps.execute(command, CreatePaperState()).paperId!!
    }

    override fun createContribution(command: CreateContributionCommand): ThingId {
        val steps = listOf(
            ContributionTempIdValidator(),
            ContributionPaperExistenceValidator(resourceRepository),
            ContributionThingDefinitionValidator(thingRepository),
            ContributionContentsValidator(thingRepository),
            ContributionContentsCreator(resourceService, statementService, literalService, predicateService, statementRepository, listService)
        )
        return steps.execute(command, ContributionState()).contributionId!!
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
