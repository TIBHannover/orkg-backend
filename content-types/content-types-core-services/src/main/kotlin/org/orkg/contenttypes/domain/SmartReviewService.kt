package org.orkg.contenttypes.domain

import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.Either
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.BibTeXReferencesValidator
import org.orkg.contenttypes.domain.actions.CreateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.CreateSmartReviewSectionCommand
import org.orkg.contenttypes.domain.actions.CreateSmartReviewSectionState
import org.orkg.contenttypes.domain.actions.CreateSmartReviewState
import org.orkg.contenttypes.domain.actions.DeleteSmartReviewSectionCommand
import org.orkg.contenttypes.domain.actions.DeleteSmartReviewSectionState
import org.orkg.contenttypes.domain.actions.DescriptionValidator
import org.orkg.contenttypes.domain.actions.LabelValidator
import org.orkg.contenttypes.domain.actions.ObservatoryValidator
import org.orkg.contenttypes.domain.actions.OrganizationValidator
import org.orkg.contenttypes.domain.actions.PublishSmartReviewCommand
import org.orkg.contenttypes.domain.actions.PublishSmartReviewState
import org.orkg.contenttypes.domain.actions.ResearchFieldValidator
import org.orkg.contenttypes.domain.actions.SDGValidator
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewSectionCommand
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewSectionState
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewState
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewAuthorCreateValidator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewAuthorCreator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewAuthorUpdateValidator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewAuthorUpdater
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewChangelogCreator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewContributionCreator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewExistenceValidator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewModifiableValidator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewPublishableValidator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewReferencesCreator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewReferencesUpdater
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewResearchFieldCreator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewResearchFieldUpdater
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewResourceCreator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewResourceUpdater
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewSDGCreator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewSDGUpdater
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewSectionsCreateValidator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewSectionsCreator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewSectionsUpdateValidator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewSectionsUpdater
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewVersionArchiver
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewVersionCreator
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewVersionDoiPublisher
import org.orkg.contenttypes.domain.actions.smartreviews.SmartReviewVersionHistoryUpdater
import org.orkg.contenttypes.domain.actions.smartreviews.sections.SmartReviewSectionCreateValidator
import org.orkg.contenttypes.domain.actions.smartreviews.sections.SmartReviewSectionCreator
import org.orkg.contenttypes.domain.actions.smartreviews.sections.SmartReviewSectionDeleter
import org.orkg.contenttypes.domain.actions.smartreviews.sections.SmartReviewSectionExistenceCreateValidator
import org.orkg.contenttypes.domain.actions.smartreviews.sections.SmartReviewSectionExistenceDeleteValidator
import org.orkg.contenttypes.domain.actions.smartreviews.sections.SmartReviewSectionExistenceUpdateValidator
import org.orkg.contenttypes.domain.actions.smartreviews.sections.SmartReviewSectionIndexValidator
import org.orkg.contenttypes.domain.actions.smartreviews.sections.SmartReviewSectionUpdateValidator
import org.orkg.contenttypes.domain.actions.smartreviews.sections.SmartReviewSectionUpdater
import org.orkg.contenttypes.input.SmartReviewUseCases
import org.orkg.contenttypes.output.ComparisonRepository
import org.orkg.contenttypes.output.DoiService
import org.orkg.contenttypes.output.SmartReviewPublishedRepository
import org.orkg.contenttypes.output.SmartReviewRepository
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ListRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class SmartReviewService(
    private val resourceRepository: ResourceRepository,
    private val smartReviewRepository: SmartReviewRepository,
    private val smartReviewPublishedRepository: SmartReviewPublishedRepository,
    private val comparisonRepository: ComparisonRepository,
    private val statementRepository: StatementRepository,
    private val observatoryRepository: ObservatoryRepository,
    private val organizationRepository: OrganizationRepository,
    private val predicateRepository: PredicateRepository,
    private val thingRepository: ThingRepository,
    private val resourceService: ResourceUseCases,
    private val literalService: LiteralUseCases,
    private val statementService: StatementUseCases,
    private val listService: ListUseCases,
    private val listRepository: ListRepository,
    private val doiService: DoiService,
    @Value("\${orkg.publishing.base-url.smart-review}")
    private val smartReviewPublishBaseUri: String = "http://localhost/review/"
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
        researchField: ThingId?,
        includeSubfields: Boolean,
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
            researchField = researchField,
            includeSubfields = includeSubfields,
            published = published,
            sustainableDevelopmentGoal = sustainableDevelopmentGoal
        ).pmap { it.toSmartReview() }

    override fun findPublishedContentById(
        smartReviewId: ThingId,
        contentId: ThingId
    ): Either<ContentType, List<GeneralStatement>> {
        val smartReview = resourceRepository.findById(smartReviewId)
            .filter { Classes.smartReviewPublished in it.classes }
            .orElseThrow { SmartReviewNotFound(smartReviewId) }
        val statements = findSubgraph(smartReview).statements
        val content = statements.values.flatten()
            .firstOrNull { statement ->
                statement.subject is Resource && SmartReviewSection.types.intersect((statement.subject as Resource).classes).isNotEmpty() &&
                    statement.`object`.id == contentId && statement.predicate.isAboutSmartReviewSection()
            }
            ?.`object`
            ?: throw PublishedSmartReviewContentNotFound(smartReviewId, contentId)
        return when {
            content is Resource && Classes.comparison in content.classes -> {
                Either.left(Comparison.from(content, statements, comparisonRepository.findVersionHistory(contentId)))
            }
            content is Resource && Classes.visualization in content.classes -> {
                Either.left(Visualization.from(content, statements))
            }
            else -> Either.right(statements[contentId].orEmpty())
        }
    }

    private fun Predicate.isAboutSmartReviewSection() =
        id == Predicates.hasLink || id == Predicates.showProperty || id == Predicates.hasEntity

    override fun create(command: CreateSmartReviewCommand): ThingId {
        val steps = listOf(
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

    override fun createSection(command: CreateSmartReviewSectionCommand): ThingId {
        val steps = listOf<Action<CreateSmartReviewSectionCommand, CreateSmartReviewSectionState>>(
            SmartReviewSectionExistenceCreateValidator(statementRepository),
            SmartReviewSectionIndexValidator(statementRepository),
            SmartReviewSectionCreateValidator(resourceRepository, predicateRepository, thingRepository),
            SmartReviewSectionCreator(literalService, resourceService, statementService)
        )
        return steps.execute(command, CreateSmartReviewSectionState()).smartReviewSectionId!!
    }

    override fun update(command: UpdateSmartReviewCommand) {
        val steps = listOf(
            SmartReviewExistenceValidator(this, resourceRepository),
            SmartReviewModifiableValidator(),
            LabelValidator("title") { it.title },
            BibTeXReferencesValidator({ it.references }),
            ResearchFieldValidator(resourceRepository, { it.researchFields }, { it.smartReview!!.researchFields.ids }),
            SmartReviewAuthorUpdateValidator(resourceRepository, statementRepository),
            SDGValidator({ it.sustainableDevelopmentGoals }, { it.smartReview!!.sustainableDevelopmentGoals.ids }),
            OrganizationValidator(organizationRepository, { it.organizations }, { it.smartReview!!.organizations }),
            ObservatoryValidator(observatoryRepository, { it.observatories }, { it.smartReview!!.observatories }),
            SmartReviewSectionsUpdateValidator(resourceRepository, predicateRepository, thingRepository),
            SmartReviewResourceUpdater(resourceService),
            SmartReviewReferencesUpdater(literalService, statementService),
            SmartReviewResearchFieldUpdater(literalService, statementService),
            SmartReviewAuthorUpdater(resourceService, statementService, literalService, listService, listRepository),
            SmartReviewSDGUpdater(literalService, statementService),
            SmartReviewSectionsUpdater(literalService, resourceService, statementService)
        )
        steps.execute(command, UpdateSmartReviewState())
    }

    override fun updateSection(command: UpdateSmartReviewSectionCommand) {
        val steps = listOf<Action<UpdateSmartReviewSectionCommand, UpdateSmartReviewSectionState>>(
            SmartReviewSectionExistenceUpdateValidator(this, resourceRepository),
            SmartReviewSectionUpdateValidator(resourceRepository, predicateRepository, thingRepository),
            SmartReviewSectionUpdater(literalService, resourceService, statementService)
        )
        steps.execute(command, UpdateSmartReviewSectionState())
    }

    override fun deleteSection(command: DeleteSmartReviewSectionCommand) {
        val steps = listOf<Action<DeleteSmartReviewSectionCommand, DeleteSmartReviewSectionState>>(
            SmartReviewSectionExistenceDeleteValidator(this, resourceRepository),
            SmartReviewSectionDeleter(statementService, resourceService)
        )
        steps.execute(command, DeleteSmartReviewSectionState())
    }

    override fun publish(command: PublishSmartReviewCommand): ThingId {
        val steps = listOf(
            SmartReviewPublishableValidator(this),
            DescriptionValidator("changelog") { it.changelog },
            DescriptionValidator { it.description?.takeIf { _ -> it.assignDOI } },
            SmartReviewVersionCreator(resourceRepository, statementRepository, resourceService, statementService, literalService, listService),
            SmartReviewChangelogCreator(literalService, statementService),
            SmartReviewVersionArchiver(statementService, smartReviewPublishedRepository),
            SmartReviewVersionHistoryUpdater(statementService),
            SmartReviewVersionDoiPublisher(statementService, literalService, doiService, smartReviewPublishBaseUri)
        )
        return steps.execute(command, PublishSmartReviewState()).smartReviewVersionId!!
    }

    internal fun findSubgraph(resource: Resource): ContentTypeSubgraph {
        var root = resource.id
        val statements = when {
            Classes.smartReviewPublished in resource.classes -> {
                val published = smartReviewPublishedRepository.findById(resource.id)
                    .orElseThrow { SmartReviewNotFound(resource.id) }
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
            Classes.smartReview in resource.classes -> {
                statementRepository.fetchAsBundle(
                    id = resource.id,
                    configuration = BundleConfiguration(
                        minLevel = null,
                        maxLevel = 3,
                        blacklist = listOf(Classes.researchField, Classes.venue),
                        whitelist = emptyList()
                    ),
                    sort = Sort.unsorted()
                ) + statementRepository.findAll(
                    subjectId = resource.id,
                    objectClasses = setOf(Classes.researchField),
                    pageable = PageRequests.ALL
                )
            }
            else -> throw IllegalStateException("""Unable to convert resource "${resource.id}" to smart review. This is a bug.""")
        }
        return ContentTypeSubgraph(root, statements.groupBy { it.subject.id })
    }

    internal fun Resource.toSmartReview(): SmartReview =
        findSubgraph(this).let { SmartReview.from(this, it.root, it.statements) }
}
