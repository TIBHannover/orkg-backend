package org.orkg.contenttypes.domain

import java.net.URI
import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.domain.actions.ContributionState
import org.orkg.contenttypes.domain.actions.CreateContributionCommand
import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.domain.actions.ObservatoryValidator
import org.orkg.contenttypes.domain.actions.OrganizationValidator
import org.orkg.contenttypes.domain.actions.PublicationInfoValidator
import org.orkg.contenttypes.domain.actions.ResearchFieldValidator
import org.orkg.contenttypes.domain.actions.SDGValidator
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.domain.actions.contribution.ContributionContentsCreator
import org.orkg.contenttypes.domain.actions.contribution.ContributionContentsValidator
import org.orkg.contenttypes.domain.actions.contribution.ContributionPaperValidator
import org.orkg.contenttypes.domain.actions.contribution.ContributionTempIdValidator
import org.orkg.contenttypes.domain.actions.contribution.ContributionThingDefinitionValidator
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.actions.paper.PaperAuthorCreateValidator
import org.orkg.contenttypes.domain.actions.paper.PaperAuthorCreator
import org.orkg.contenttypes.domain.actions.paper.PaperAuthorUpdateValidator
import org.orkg.contenttypes.domain.actions.paper.PaperAuthorUpdater
import org.orkg.contenttypes.domain.actions.paper.PaperContributionCreator
import org.orkg.contenttypes.domain.actions.paper.PaperContributionValidator
import org.orkg.contenttypes.domain.actions.paper.PaperExistenceValidator
import org.orkg.contenttypes.domain.actions.paper.PaperIdentifierCreateValidator
import org.orkg.contenttypes.domain.actions.paper.PaperIdentifierCreator
import org.orkg.contenttypes.domain.actions.paper.PaperIdentifierUpdateValidator
import org.orkg.contenttypes.domain.actions.paper.PaperIdentifierUpdater
import org.orkg.contenttypes.domain.actions.paper.PaperModifiableValidator
import org.orkg.contenttypes.domain.actions.paper.PaperPublicationInfoCreator
import org.orkg.contenttypes.domain.actions.paper.PaperPublicationInfoUpdater
import org.orkg.contenttypes.domain.actions.paper.PaperResearchFieldCreator
import org.orkg.contenttypes.domain.actions.paper.PaperResearchFieldUpdater
import org.orkg.contenttypes.domain.actions.paper.PaperResourceCreator
import org.orkg.contenttypes.domain.actions.paper.PaperResourceUpdater
import org.orkg.contenttypes.domain.actions.paper.PaperSDGCreator
import org.orkg.contenttypes.domain.actions.paper.PaperTempIdValidator
import org.orkg.contenttypes.domain.actions.paper.PaperThingDefinitionValidator
import org.orkg.contenttypes.domain.actions.paper.PaperTitleCreateValidator
import org.orkg.contenttypes.domain.actions.paper.PaperTitleUpdateValidator
import org.orkg.contenttypes.input.PaperUseCases
import org.orkg.contenttypes.input.PublishPaperUseCase
import org.orkg.contenttypes.output.PaperRepository
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
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
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class PaperService(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val observatoryRepository: ObservatoryRepository,
    private val organizationRepository: OrganizationRepository,
    private val thingRepository: ThingRepository,
    private val resourceService: ResourceUseCases,
    private val statementService: StatementUseCases,
    private val literalService: LiteralUseCases,
    private val predicateService: PredicateUseCases,
    private val listService: ListUseCases,
    private val publishingService: PublishingService,
    private val paperRepository: PaperRepository,
    @Value("\${orkg.publishing.base-url.paper}")
    private val paperPublishBaseUri: String = "http://localhost/paper/"
) : PaperUseCases {
    override fun findById(id: ThingId): Optional<Paper> =
        resourceRepository.findPaperById(id)
            .map { it.toPaper() }

    override fun findAll(
        pageable: Pageable,
        label: SearchString?,
        doi: String?,
        visibility: VisibilityFilter?,
        verified: Boolean?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
        researchField: ThingId?,
        includeSubfields: Boolean,
        sustainableDevelopmentGoal: ThingId?
    ): Page<Paper> =
        paperRepository.findAll(
            pageable = pageable,
            label = label,
            doi = doi,
            visibility = visibility,
            verified = verified,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            researchField = researchField,
            includeSubfields = includeSubfields,
            sustainableDevelopmentGoal = sustainableDevelopmentGoal
        ).pmap { it.toPaper() }

    override fun findAllContributorsByPaperId(id: ThingId, pageable: Pageable): Page<ContributorId> =
        resourceRepository.findPaperById(id)
            .map { statementRepository.findAllContributorsByResourceId(id, pageable) }
            .orElseThrow { PaperNotFound(id) }

    override fun create(command: CreatePaperCommand): ThingId {
        val steps = listOf(
            PaperTempIdValidator(),
            PublicationInfoValidator { it.publicationInfo },
            PaperTitleCreateValidator(resourceService),
            PaperIdentifierCreateValidator(statementRepository),
            ResearchFieldValidator(resourceRepository, { it.researchFields }),
            ObservatoryValidator(observatoryRepository, { it.observatories }),
            OrganizationValidator(organizationRepository, { it.organizations }),
            SDGValidator({ it.sustainableDevelopmentGoals }),
            PaperAuthorCreateValidator(resourceRepository, statementRepository),
            PaperThingDefinitionValidator(thingRepository),
            PaperContributionValidator(thingRepository),
            PaperResourceCreator(resourceService),
            PaperIdentifierCreator(statementService, literalService),
            PaperSDGCreator(statementService),
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
            ContributionPaperValidator(resourceRepository),
            ContributionThingDefinitionValidator(thingRepository),
            ContributionContentsValidator(thingRepository),
            ContributionContentsCreator(resourceService, statementService, literalService, predicateService, statementRepository, listService)
        )
        return steps.execute(command, ContributionState()).contributionId!!
    }

    override fun update(command: UpdatePaperCommand) {
        val steps = listOf(
            PaperExistenceValidator(this),
            PaperModifiableValidator(),
            PublicationInfoValidator { it.publicationInfo },
            ResearchFieldValidator(resourceRepository, { it.researchFields }, { it.paper!!.researchFields.map(ObjectIdAndLabel::id) }),
            ObservatoryValidator(observatoryRepository, { it.observatories }, { it.paper!!.observatories }),
            OrganizationValidator(organizationRepository, { it.organizations }, { it.paper!!.organizations }),
            PaperTitleUpdateValidator(resourceService),
            PaperIdentifierUpdateValidator(statementRepository),
            PaperAuthorUpdateValidator(resourceRepository, statementRepository),
            PaperResourceUpdater(resourceService),
            PaperIdentifierUpdater(statementService, literalService),
            PaperAuthorUpdater(resourceService, statementService, literalService, listService),
            PaperResearchFieldUpdater(statementService),
            PaperPublicationInfoUpdater(resourceService, resourceRepository, statementService, literalService)
        )
        steps.execute(command, UpdatePaperState())
    }

    override fun publish(command: PublishPaperUseCase.PublishCommand) {
        val paper = resourceRepository.findPaperById(command.id)
            .orElseThrow { PaperNotFound(command.id) }
        publishingService.publish(
            PublishingService.PublishCommand(
                id = paper.id,
                title = paper.label,
                contributorId = command.contributorId,
                subject = command.subject,
                description = command.description,
                url = URI.create("$paperPublishBaseUri/").resolve(paper.id.value),
                creators = command.authors,
                resourceType = Classes.paper,
                relatedIdentifiers = emptyList()
            )
        )
    }

    private fun Resource.toPaper(): Paper {
        val statements = (
            statementRepository.fetchAsBundle(
                id = id,
                configuration = BundleConfiguration(
                    minLevel = null,
                    maxLevel = 3,
                    blacklist = listOf(Classes.researchField, Classes.contribution, Classes.venue),
                    whitelist = emptyList()
                ),
                sort = Sort.unsorted()
            ) + statementRepository.fetchAsBundle(
                id = id,
                configuration = BundleConfiguration(
                    minLevel = null,
                    maxLevel = 1,
                    blacklist = emptyList(),
                    whitelist = listOf(Classes.researchField, Classes.contribution, Classes.venue)
                ),
                sort = Sort.unsorted()
            )
        ).groupBy { it.subject.id }
        val directStatements = statements[id].orEmpty()
        return Paper(
            id = id,
            title = label,
            researchFields = directStatements.wherePredicate(Predicates.hasResearchField)
                .objectIdsAndLabel()
                .sortedBy { it.id },
            identifiers = directStatements.associateIdentifiers(Identifiers.paper),
            publicationInfo = PublicationInfo.from(directStatements),
            authors = statements.authors(id),
            contributions = directStatements.wherePredicate(Predicates.hasContribution).objectIdsAndLabel(),
            sustainableDevelopmentGoals = directStatements.wherePredicate(Predicates.sustainableDevelopmentGoal)
                .objectIdsAndLabel()
                .sortedBy { it.id }
                .toSet(),
            observatories = listOf(observatoryId),
            organizations = listOf(organizationId),
            extractionMethod = extractionMethod,
            createdAt = createdAt,
            createdBy = createdBy,
            verified = verified ?: false,
            visibility = visibility,
            modifiable = modifiable,
            unlistedBy = unlistedBy
        )
    }
}
