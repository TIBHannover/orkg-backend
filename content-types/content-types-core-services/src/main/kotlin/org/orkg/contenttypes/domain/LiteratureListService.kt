package org.orkg.contenttypes.domain

import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.LiteratureListUseCases
import org.orkg.contenttypes.output.LiteratureListPublishedRepository
import org.orkg.contenttypes.output.LiteratureListRepository
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.authors
import org.orkg.graph.output.legacyAuthors
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class LiteratureListService(
    private val resourceRepository: ResourceRepository,
    private val literatureListRepository: LiteratureListRepository,
    private val literatureListPublishedRepository: LiteratureListPublishedRepository,
    private val statementRepository: StatementRepository
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

    private fun Resource.toLiteratureList(): LiteratureList {
        var root = id
        val statements = when {
            Classes.literatureListPublished in classes -> {
                val published = literatureListPublishedRepository.findById(id)
                    .orElseThrow { LiteratureListNotFound(id) }
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
            Classes.literatureList in classes -> {
                statementRepository.fetchAsBundle(
                    id = id,
                    configuration = BundleConfiguration(
                        minLevel = null,
                        maxLevel = 3,
                        blacklist = listOf(Classes.researchField, Classes.contribution, Classes.venue),
                        whitelist = emptyList()
                    ),
                    sort = Sort.unsorted()
                )
            }
            else -> throw IllegalStateException("""Unable to convert resource "$id" to literature list. This is a bug.""")
        }.groupBy { it.subject.id }
        val directStatements = statements[root].orEmpty()
        return LiteratureList(
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
            published = Classes.literatureListPublished in classes,
            sections = directStatements.wherePredicate(Predicates.hasSection)
                .filter { it.`object` is Resource }
                .sortedBy { it.createdAt }
                .map { LiteratureListSection.from(it.`object` as Resource, statements) }
        )
    }
}
