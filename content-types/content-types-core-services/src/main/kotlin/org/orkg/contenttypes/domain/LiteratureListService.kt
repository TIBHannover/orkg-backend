package org.orkg.contenttypes.domain

import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.domain.actions.CreateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.CreateLiteratureListState
import org.orkg.contenttypes.domain.actions.LabelValidator
import org.orkg.contenttypes.domain.actions.ObservatoryValidator
import org.orkg.contenttypes.domain.actions.OrganizationValidator
import org.orkg.contenttypes.domain.actions.ResearchFieldValidator
import org.orkg.contenttypes.domain.actions.SDGValidator
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListState
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.actions.literaturelists.LiteratureListAuthorCreateValidator
import org.orkg.contenttypes.domain.actions.literaturelists.LiteratureListAuthorCreator
import org.orkg.contenttypes.domain.actions.literaturelists.LiteratureListAuthorUpdateValidator
import org.orkg.contenttypes.domain.actions.literaturelists.LiteratureListAuthorUpdater
import org.orkg.contenttypes.domain.actions.literaturelists.LiteratureListExistenceValidator
import org.orkg.contenttypes.domain.actions.literaturelists.LiteratureListModifiableValidator
import org.orkg.contenttypes.domain.actions.literaturelists.LiteratureListResearchFieldCreator
import org.orkg.contenttypes.domain.actions.literaturelists.LiteratureListResearchFieldUpdater
import org.orkg.contenttypes.domain.actions.literaturelists.LiteratureListResourceCreator
import org.orkg.contenttypes.domain.actions.literaturelists.LiteratureListResourceUpdater
import org.orkg.contenttypes.domain.actions.literaturelists.LiteratureListSDGCreator
import org.orkg.contenttypes.domain.actions.literaturelists.LiteratureListSDGUpdater
import org.orkg.contenttypes.domain.actions.literaturelists.LiteratureListSectionsCreateValidator
import org.orkg.contenttypes.domain.actions.literaturelists.LiteratureListSectionsCreator
import org.orkg.contenttypes.domain.actions.literaturelists.LiteratureListSectionsUpdateValidator
import org.orkg.contenttypes.domain.actions.literaturelists.LiteratureListSectionsUpdater
import org.orkg.contenttypes.input.LiteratureListUseCases
import org.orkg.contenttypes.output.LiteratureListPublishedRepository
import org.orkg.contenttypes.output.LiteratureListRepository
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class LiteratureListService(
    private val resourceRepository: ResourceRepository,
    private val literatureListRepository: LiteratureListRepository,
    private val literatureListPublishedRepository: LiteratureListPublishedRepository,
    private val statementRepository: StatementRepository,
    private val observatoryRepository: ObservatoryRepository,
    private val organizationRepository: OrganizationRepository,
    private val resourceService: ResourceUseCases,
    private val literalService: LiteralUseCases,
    private val statementService: StatementUseCases,
    private val listService: ListUseCases
) : LiteratureListUseCases {
    override fun findById(id: ThingId): Optional<LiteratureList> =
        resourceRepository.findById(id)
            .filter { Classes.literatureList in it.classes || Classes.literatureListPublished in it.classes }
            .map { it.toLiteratureList() }

    override fun findAll(
        pageable: Pageable,
        label: SearchString?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
        published: Boolean?,
        sustainableDevelopmentGoal: ThingId?
    ): Page<LiteratureList> =
        literatureListRepository.findAll(
            pageable = pageable,
            label = label,
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            published = published,
            sustainableDevelopmentGoal = sustainableDevelopmentGoal
        ).pmap { it.toLiteratureList() }

    override fun create(command: CreateLiteratureListCommand): ThingId {
        val steps = listOf(
            LabelValidator("title") { it.title },
            ResearchFieldValidator(resourceRepository, { it.researchFields }),
            LiteratureListAuthorCreateValidator(resourceRepository, statementRepository),
            SDGValidator({ it.sustainableDevelopmentGoals }),
            OrganizationValidator(organizationRepository, { it.organizations }),
            ObservatoryValidator(observatoryRepository, { it.observatories }),
            LiteratureListSectionsCreateValidator(resourceRepository),
            LiteratureListResourceCreator(resourceService),
            LiteratureListResearchFieldCreator(literalService, statementService),
            LiteratureListAuthorCreator(resourceService, statementService, literalService, listService),
            LiteratureListSDGCreator(literalService, statementService),
            LiteratureListSectionsCreator(literalService, resourceService, statementService)
        )
        return steps.execute(command, CreateLiteratureListState()).literatureListId!!
    }

    override fun update(command: UpdateLiteratureListCommand) {
        val steps = listOf(
            LiteratureListExistenceValidator(this, resourceRepository),
            LiteratureListModifiableValidator(),
            LabelValidator("title") { it.title },
            ResearchFieldValidator(resourceRepository, { it.researchFields }, { it.literatureList!!.researchFields.ids }),
            LiteratureListAuthorUpdateValidator(resourceRepository, statementRepository),
            SDGValidator({ it.sustainableDevelopmentGoals }, { it.literatureList!!.sustainableDevelopmentGoals.ids }),
            OrganizationValidator(organizationRepository, { it.organizations }, { it.literatureList!!.organizations }),
            ObservatoryValidator(observatoryRepository, { it.observatories }, { it.literatureList!!.observatories }),
            LiteratureListSectionsUpdateValidator(resourceRepository),
            LiteratureListResourceUpdater(resourceService),
            LiteratureListResearchFieldUpdater(literalService, statementService),
            LiteratureListAuthorUpdater(resourceService, statementService, literalService, listService),
            LiteratureListSDGUpdater(literalService, statementService),
            LiteratureListSectionsUpdater(literalService, resourceService, statementService)
        )
        steps.execute(command, UpdateLiteratureListState())
    }

    internal fun findSubgraph(resource: Resource): ContentTypeSubgraph {
        var root = resource.id
        val statements = when {
            Classes.literatureListPublished in resource.classes -> {
                val published = literatureListPublishedRepository.findById(resource.id)
                    .orElseThrow { LiteratureListNotFound(resource.id) }
                root = published.rootId
                val versions = statementRepository.fetchAsBundle(
                    id = root,
                    configuration = BundleConfiguration(
                        minLevel = null,
                        maxLevel = 2,
                        blacklist = emptyList(),
                        whitelist = listOf(Classes.literatureList, Classes.literatureListPublished, Classes.literal)
                    ),
                    sort = Sort.unsorted()
                )
                published.subgraph.filter { it.predicate.id != Predicates.hasPublishedVersion } + versions
            }
            Classes.literatureList in resource.classes -> {
                statementRepository.fetchAsBundle(
                    id = resource.id,
                    configuration = BundleConfiguration(
                        minLevel = null,
                        maxLevel = 3,
                        blacklist = listOf(Classes.researchField, Classes.contribution, Classes.venue),
                        whitelist = emptyList()
                    ),
                    sort = Sort.unsorted()
                ) + statementRepository.findAll(
                    subjectId = resource.id,
                    objectClasses = setOf(Classes.researchField),
                    pageable = PageRequests.ALL
                )
            }
            else -> throw IllegalStateException("""Unable to convert resource "${resource.id}" to literature list. This is a bug.""")
        }
        return ContentTypeSubgraph(root, statements.groupBy { it.subject.id })
    }

    internal fun Resource.toLiteratureList(): LiteratureList =
        findSubgraph(this).let { LiteratureList.from(this, it.root, it.statements) }
}
