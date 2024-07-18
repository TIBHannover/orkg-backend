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
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.BibTeXReferencesValidator
import org.orkg.contenttypes.domain.actions.CreateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.CreateSmartReviewState
import org.orkg.contenttypes.domain.actions.LabelValidator
import org.orkg.contenttypes.domain.actions.ObservatoryValidator
import org.orkg.contenttypes.domain.actions.OrganizationValidator
import org.orkg.contenttypes.domain.actions.ResearchFieldValidator
import org.orkg.contenttypes.domain.actions.SDGValidator
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewAuthorCreateValidator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewAuthorCreator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewContributionCreator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewReferencesCreator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewResearchFieldCreator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewResourceCreator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewSDGCreator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewSectionsCreateValidator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewSectionsCreator
import org.orkg.contenttypes.input.SmartReviewUseCases
import org.orkg.contenttypes.output.SmartReviewPublishedRepository
import org.orkg.contenttypes.output.SmartReviewRepository
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class SmartReviewService(
    private val resourceRepository: ResourceRepository,
    private val smartReviewRepository: SmartReviewRepository,
    private val smartReviewPublishedRepository: SmartReviewPublishedRepository,
    private val statementRepository: StatementRepository,
    private val observatoryRepository: ObservatoryRepository,
    private val organizationRepository: OrganizationRepository,
    private val predicateRepository: PredicateRepository,
    private val thingRepository: ThingRepository,
    private val resourceService: ResourceUseCases,
    private val literalService: LiteralUseCases,
    private val statementService: StatementUseCases,
    private val listService: ListUseCases,
) : SmartReviewUseCases {
    override fun findById(id: ThingId): Optional<SmartReview> =
        resourceRepository.findById(id)
            .filter { Classes.smartReview in it.classes || Classes.smartReviewPublished in it.classes }
            .map { it.toSmartReview() }

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
    ): Page<SmartReview> =
        smartReviewRepository.findAll(
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
        ).pmap { it.toSmartReview() }

    override fun create(command: CreateSmartReviewCommand): ThingId {
        val steps = listOf<Action<CreateSmartReviewCommand, CreateSmartReviewState>>(
            LabelValidator("title") { it.title },
            BibTeXReferencesValidator({ it.references }),
            ResearchFieldValidator(resourceRepository, { it.researchFields }),
            SmartReviewAuthorCreateValidator(resourceRepository, statementRepository),
            SDGValidator({ it.sustainableDevelopmentGoals }),
            OrganizationValidator(organizationRepository, { it.organizations }),
            ObservatoryValidator(observatoryRepository, { it.observatories }),
            SmartReviewSectionsCreateValidator(resourceRepository, predicateRepository, thingRepository),
            SmartReviewResourceCreator(resourceService),
            SmartReviewContributionCreator(resourceService, statementService),
            SmartReviewReferencesCreator(literalService, statementService),
            SmartReviewResearchFieldCreator(literalService, statementService),
            SmartReviewAuthorCreator(resourceService, statementService, literalService, listService),
            SmartReviewSDGCreator(literalService, statementService),
            SmartReviewSectionsCreator(literalService, resourceService, statementService)
        )
        return steps.execute(command, CreateSmartReviewState()).smartReviewId!!
    }

    internal fun Resource.toSmartReview(): SmartReview {
        var root = id
        val statements = when {
            Classes.smartReviewPublished in classes -> {
                val published = smartReviewPublishedRepository.findById(id)
                    .orElseThrow { SmartReviewNotFound(id) }
                root = published.rootId
                val versions = statementRepository.fetchAsBundle(
                    id = root,
                    configuration = BundleConfiguration(
                        minLevel = null,
                        maxLevel = 2,
                        blacklist = emptyList(),
                        whitelist = listOf(Classes.smartReview, Classes.smartReviewPublished, Classes.literal)
                    ),
                    sort = Sort.unsorted()
                )
                published.subgraph.filter { it.predicate.id != Predicates.hasPublishedVersion } + versions
            }
            Classes.smartReview in classes -> {
                statementRepository.fetchAsBundle(
                    id = id,
                    configuration = BundleConfiguration(
                        minLevel = null,
                        maxLevel = 3,
                        blacklist = listOf(Classes.researchField, Classes.venue),
                        whitelist = emptyList()
                    ),
                    sort = Sort.unsorted()
                ) + statementRepository.findAll(
                    subjectId = id,
                    objectClasses = setOf(Classes.researchField),
                    pageable = PageRequests.ALL
                )
            }
            else -> throw IllegalStateException("""Unable to convert resource "$id" to smart review. This is a bug.""")
        }.groupBy { it.subject.id }
        val directStatements = statements[root].orEmpty()
        val contributionStatements = directStatements.singleOrNull {
                it.predicate.id == Predicates.hasContribution && it.`object` is Resource && Classes.contributionSmartReview in (it.`object` as Resource).classes
            }
            ?.let { statements[it.`object`.id] }
            .orEmpty()
        return SmartReview(
            id = id,
            title = label,
            researchFields = directStatements.wherePredicate(Predicates.hasResearchField)
                .objectIdsAndLabel()
                .sortedBy { it.id },
            authors = statements.authors(root).ifEmpty { statements.legacyAuthors(root) },
            versions = VersionInfo(
                head = HeadVersion(directStatements.firstOrNull()?.subject ?: this),
                published = directStatements.wherePredicate(Predicates.hasPublishedVersion)
                    .sortedByDescending { it.createdAt }
                    .objects()
                    .map { PublishedVersion(it, statements[it.id]?.wherePredicate(Predicates.description)?.firstObjectLabel()) }
            ),
            sustainableDevelopmentGoals = directStatements.wherePredicate(Predicates.sustainableDevelopmentGoal)
                .objectIdsAndLabel()
                .sortedBy { it.id }
                .toSet(),
            observatories = listOf(observatoryId),
            organizations = listOf(organizationId),
            extractionMethod = extractionMethod,
            createdAt = createdAt,
            createdBy = createdBy,
            visibility = visibility,
            unlistedBy = unlistedBy,
            published = Classes.smartReviewPublished in classes,
            sections = contributionStatements.wherePredicate(Predicates.hasSection)
                .filter { it.`object` is Resource }
                .sortedBy { it.createdAt }
                .map { SmartReviewSection.from(it.`object` as Resource, statements) },
            references = contributionStatements.wherePredicate(Predicates.hasReference)
                .filter { it.`object` is Literal }
                .sortedBy { it.createdAt }
                .map { it.`object`.label }
        )
    }
}
