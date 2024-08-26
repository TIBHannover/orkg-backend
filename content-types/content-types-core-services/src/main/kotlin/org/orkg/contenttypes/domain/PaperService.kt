package org.orkg.contenttypes.domain

import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.ContributionState
import org.orkg.contenttypes.domain.actions.CreateContributionCommand
import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.domain.actions.ObservatoryValidator
import org.orkg.contenttypes.domain.actions.OrganizationValidator
import org.orkg.contenttypes.domain.actions.PublicationInfoValidator
import org.orkg.contenttypes.domain.actions.PublishPaperCommand
import org.orkg.contenttypes.domain.actions.PublishPaperState
import org.orkg.contenttypes.domain.actions.ResearchFieldValidator
import org.orkg.contenttypes.domain.actions.ResourceValidator
import org.orkg.contenttypes.domain.actions.SDGValidator
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.domain.actions.contributions.ContributionContentsCreator
import org.orkg.contenttypes.domain.actions.contributions.ContributionContentsValidator
import org.orkg.contenttypes.domain.actions.contributions.ContributionPaperValidator
import org.orkg.contenttypes.domain.actions.contributions.ContributionTempIdValidator
import org.orkg.contenttypes.domain.actions.contributions.ContributionThingDefinitionValidator
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.actions.papers.PaperAuthorCreateValidator
import org.orkg.contenttypes.domain.actions.papers.PaperAuthorCreator
import org.orkg.contenttypes.domain.actions.papers.PaperAuthorUpdateValidator
import org.orkg.contenttypes.domain.actions.papers.PaperAuthorUpdater
import org.orkg.contenttypes.domain.actions.papers.PaperContributionCreator
import org.orkg.contenttypes.domain.actions.papers.PaperContributionValidator
import org.orkg.contenttypes.domain.actions.papers.PaperExistenceValidator
import org.orkg.contenttypes.domain.actions.papers.PaperIdentifierCreateValidator
import org.orkg.contenttypes.domain.actions.papers.PaperIdentifierCreator
import org.orkg.contenttypes.domain.actions.papers.PaperIdentifierUpdateValidator
import org.orkg.contenttypes.domain.actions.papers.PaperIdentifierUpdater
import org.orkg.contenttypes.domain.actions.papers.PaperMentioningsCreator
import org.orkg.contenttypes.domain.actions.papers.PaperMentioningsUpdater
import org.orkg.contenttypes.domain.actions.papers.PaperModifiableValidator
import org.orkg.contenttypes.domain.actions.papers.PaperPublicationInfoCreator
import org.orkg.contenttypes.domain.actions.papers.PaperPublicationInfoUpdater
import org.orkg.contenttypes.domain.actions.papers.PaperPublishableValidator
import org.orkg.contenttypes.domain.actions.papers.PaperResearchFieldCreator
import org.orkg.contenttypes.domain.actions.papers.PaperResearchFieldUpdater
import org.orkg.contenttypes.domain.actions.papers.PaperResourceCreator
import org.orkg.contenttypes.domain.actions.papers.PaperResourceUpdater
import org.orkg.contenttypes.domain.actions.papers.PaperSDGCreator
import org.orkg.contenttypes.domain.actions.papers.PaperSDGUpdater
import org.orkg.contenttypes.domain.actions.papers.PaperTempIdValidator
import org.orkg.contenttypes.domain.actions.papers.PaperThingDefinitionValidator
import org.orkg.contenttypes.domain.actions.papers.PaperTitleCreateValidator
import org.orkg.contenttypes.domain.actions.papers.PaperTitleUpdateValidator
import org.orkg.contenttypes.domain.actions.papers.PaperVersionArchiver
import org.orkg.contenttypes.domain.actions.papers.PaperVersionCreator
import org.orkg.contenttypes.domain.actions.papers.PaperVersionDoiPublisher
import org.orkg.contenttypes.domain.actions.papers.PaperVersionHistoryUpdater
import org.orkg.contenttypes.input.PaperUseCases
import org.orkg.contenttypes.output.DoiService
import org.orkg.contenttypes.output.PaperPublishedRepository
import org.orkg.contenttypes.output.PaperRepository
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ListRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
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
    private val classService: ClassUseCases,
    private val resourceService: ResourceUseCases,
    private val statementService: StatementUseCases,
    private val literalService: LiteralUseCases,
    private val predicateService: PredicateUseCases,
    private val listService: ListUseCases,
    private val listRepository: ListRepository,
    private val doiService: DoiService,
    private val paperRepository: PaperRepository,
    private val classRepository: ClassRepository,
    private val paperPublishedRepository: PaperPublishedRepository,
    @Value("\${orkg.publishing.base-url.paper}")
    private val paperPublishBaseUri: String = "http://localhost/paper/"
) : PaperUseCases {

    override fun countAllStatementsAboutPapers(pageable: Pageable): Page<PaperWithStatementCount> {
        return paperRepository.findAll(pageable = pageable).pmap { paper ->
            val totalStatementCount = statementRepository.countByIdRecursive(paper.id)
            PaperWithStatementCount(paper.id, paper.label, totalStatementCount)
        }
    }

    override fun findById(id: ThingId): Optional<Paper> =
        resourceRepository.findPaperById(id)
            .map { it.toPaper() }

    override fun findAll(
        pageable: Pageable,
        label: SearchString?,
        doi: String?,
        doiPrefix: String?,
        visibility: VisibilityFilter?,
        verified: Boolean?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
        researchField: ThingId?,
        includeSubfields: Boolean,
        sustainableDevelopmentGoal: ThingId?,
        mentionings: Set<ThingId>?
    ): Page<Paper> =
        paperRepository.findAll(
            pageable = pageable,
            label = label,
            doi = doi,
            doiPrefix = doiPrefix,
            visibility = visibility,
            verified = verified,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            researchField = researchField,
            includeSubfields = includeSubfields,
            sustainableDevelopmentGoal = sustainableDevelopmentGoal,
            mentionings = mentionings
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
            ResourceValidator(resourceRepository, { it.mentionings }),
            PaperAuthorCreateValidator(resourceRepository, statementRepository),
            PaperThingDefinitionValidator(thingRepository, classRepository),
            PaperContributionValidator(thingRepository),
            PaperResourceCreator(resourceService),
            PaperIdentifierCreator(statementService, literalService),
            PaperSDGCreator(literalService, statementService),
            PaperMentioningsCreator(literalService, statementService),
            PaperAuthorCreator(resourceService, statementService, literalService, listService),
            PaperResearchFieldCreator(literalService, statementService),
            PaperPublicationInfoCreator(resourceService, resourceRepository, statementService, literalService),
            PaperContributionCreator(classService, resourceService, statementService, literalService, predicateService, statementRepository, listService)
        )
        return steps.execute(command, CreatePaperState()).paperId!!
    }

    override fun createContribution(command: CreateContributionCommand): ThingId {
        val steps = listOf(
            ContributionTempIdValidator(),
            ContributionPaperValidator(resourceRepository),
            ContributionThingDefinitionValidator(thingRepository, classRepository),
            ContributionContentsValidator(thingRepository),
            ContributionContentsCreator(classService, resourceService, statementService, literalService, predicateService, statementRepository, listService)
        )
        return steps.execute(command, ContributionState()).contributionId!!
    }

    override fun update(command: UpdatePaperCommand) {
        val steps = listOf(
            PaperExistenceValidator(this, resourceRepository),
            PaperModifiableValidator(),
            PublicationInfoValidator { it.publicationInfo },
            ResearchFieldValidator(resourceRepository, { it.researchFields }, { it.paper!!.researchFields.ids }),
            ObservatoryValidator(observatoryRepository, { it.observatories }, { it.paper!!.observatories }),
            OrganizationValidator(organizationRepository, { it.organizations }, { it.paper!!.organizations }),
            SDGValidator({ it.sustainableDevelopmentGoals }, { it.paper!!.sustainableDevelopmentGoals.ids }),
            ResourceValidator(resourceRepository, { it.mentionings }, { it.paper!!.mentionings.ids }),
            PaperTitleUpdateValidator(resourceService),
            PaperIdentifierUpdateValidator(statementRepository),
            PaperAuthorUpdateValidator(resourceRepository, statementRepository),
            PaperResourceUpdater(resourceService),
            PaperIdentifierUpdater(statementService, literalService),
            PaperAuthorUpdater(resourceService, statementService, literalService, listService, listRepository),
            PaperResearchFieldUpdater(literalService, statementService),
            PaperPublicationInfoUpdater(resourceService, resourceRepository, statementService, literalService),
            PaperSDGUpdater(literalService, statementService),
            PaperMentioningsUpdater(literalService, statementService)
        )
        steps.execute(command, UpdatePaperState())
    }

    override fun publish(command: PublishPaperCommand): ThingId {
        val steps = listOf<Action<PublishPaperCommand, PublishPaperState>>(
            PaperPublishableValidator(this, resourceRepository),
            PaperVersionCreator(resourceRepository, statementRepository, resourceService, statementService, literalService, listService),
            PaperVersionArchiver(statementService, paperPublishedRepository),
            PaperVersionHistoryUpdater(statementService),
            PaperVersionDoiPublisher(statementService, literalService, doiService, paperPublishBaseUri)
        )
        return steps.execute(command, PublishPaperState()).paperVersionId!!
    }

    internal fun findSubgraph(resource: Resource): ContentTypeSubgraph {
        val statements = statementRepository.fetchAsBundle(
            id = resource.id,
            configuration = BundleConfiguration(
                minLevel = null,
                maxLevel = 3,
                blacklist = listOf(Classes.researchField, Classes.contribution, Classes.venue),
                whitelist = emptyList()
            ),
            sort = Sort.unsorted()
        ) + statementRepository.fetchAsBundle(
            id = resource.id,
            configuration = BundleConfiguration(
                minLevel = null,
                maxLevel = 1,
                blacklist = emptyList(),
                whitelist = listOf(Classes.researchField, Classes.contribution, Classes.venue)
            ),
            sort = Sort.unsorted()
        )
        return ContentTypeSubgraph(
            root = resource.id,
            statements = statements.groupBy { it.subject.id }
        )
    }

    internal fun Resource.toPaper(): Paper {
        return Paper.from(this, findSubgraph(this).statements)
    }
}
