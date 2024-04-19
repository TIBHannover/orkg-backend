package org.orkg.contenttypes.domain

import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
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
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class SmartReviewService(
    private val resourceRepository: ResourceRepository,
    private val smartReviewRepository: SmartReviewRepository,
    private val smartReviewPublishedRepository: SmartReviewPublishedRepository,
    private val statementRepository: StatementRepository
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
                head = HeadVersion(directStatements.first().subject as Resource),
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
