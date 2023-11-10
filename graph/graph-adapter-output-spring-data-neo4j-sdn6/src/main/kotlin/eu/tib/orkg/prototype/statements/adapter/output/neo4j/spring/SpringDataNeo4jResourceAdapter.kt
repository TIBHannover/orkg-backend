package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResourceIdGenerator
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResourceRepository
import eu.tib.orkg.prototype.statements.api.VisibilityFilter
import eu.tib.orkg.prototype.statements.domain.model.ExactSearchString
import eu.tib.orkg.prototype.statements.domain.model.FuzzySearchString
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

const val RESOURCE_ID_TO_RESOURCE_CACHE = "resource-id-to-resource"
const val RESOURCE_ID_TO_RESOURCE_EXISTS_CACHE = "resource-id-to-resource-exists"

@Component
@CacheConfig(cacheNames = [RESOURCE_ID_TO_RESOURCE_CACHE, RESOURCE_ID_TO_RESOURCE_EXISTS_CACHE])
class SpringDataNeo4jResourceAdapter(
    private val neo4jRepository: Neo4jResourceRepository,
    private val neo4jResourceIdGenerator: Neo4jResourceIdGenerator,
    private val neo4jClient: Neo4jClient
) : ResourceRepository {
    override fun findByIdAndClasses(id: ThingId, classes: Set<ThingId>): Resource? =
        neo4jRepository.findByIdAndClassesContaining(id, classes)?.toResource()

    override fun nextIdentity(): ThingId {
        // IDs could exist already by manual creation. We need to find the next available one.
        var id: ThingId
        do {
            id = neo4jResourceIdGenerator.nextIdentity()
        } while (neo4jRepository.existsById(id))
        return id
    }

    @Caching(
        evict = [
            CacheEvict(key = "#resource.id", cacheNames = [RESOURCE_ID_TO_RESOURCE_CACHE]),
            CacheEvict(key = "#resource.id", cacheNames = [THING_ID_TO_THING_CACHE]),
        ]
    )
    override fun save(resource: Resource) {
        neo4jRepository.save(resource.toNeo4jResource())
    }

    @Caching(
        evict = [
            CacheEvict(key = "#id"),
            CacheEvict(key = "#id", cacheNames = [THING_ID_TO_THING_CACHE]),
        ]
    )
    override fun deleteById(id: ThingId) {
        neo4jRepository.deleteById(id)
    }

    @Caching(
        evict = [
            CacheEvict(allEntries = true),
            CacheEvict(allEntries = true, cacheNames = [THING_ID_TO_THING_CACHE]),
        ]
    )
    override fun deleteAll() {
        neo4jRepository.deleteAll()
    }

    override fun findAll(pageable: Pageable): Page<Resource> =
        findAllWithFilters(pageable = pageable)

    override fun findAllWithFilters(
        classes: Set<ThingId>,
        visibility: VisibilityFilter?,
        organizationId: OrganizationId?,
        observatoryId: ObservatoryId?,
        createdBy: ContributorId?,
        createdAt: OffsetDateTime?,
        pageable: Pageable
    ): Page<Resource> {
        val where = buildString {
            if (visibility != null) {
                if (visibility == VisibilityFilter.ALL_LISTED) {
                    append(""" AND (n.visibility = "DEFAULT" OR n.visibility = "FEATURED")""")
                } else {
                    append(" AND n.visibility = ${'$'}visibility")
                }
            }
            if (organizationId != null) {
                append(" AND n.organization_id = ${'$'}organizationId")
            }
            if (observatoryId != null) {
                append(" AND n.observatory_id = ${'$'}observatoryId")
            }
            if (createdBy != null) {
                append(" AND n.created_by = ${'$'}createdBy")
            }
            if (createdAt != null) {
                append(" AND n.created_at = ${'$'}createdAt")
            }
            appendOrderByOptimizations(pageable, createdAt, createdBy)
        }.replaceFirst(" AND", "WHERE")

        val classLabels = classes.joinToString(separator = "") { ":`$it`" }
        val query = """
            MATCH (n:Resource$classLabels) $where
            RETURN n, n.id AS id, n.label AS label, n.created_by AS created_by, n.created_at AS created_at
            SKIP ${'$'}skip LIMIT ${'$'}limit""".sortedWith(pageable.sort).trimIndent()
        val countQuery = """
            MATCH (n:Resource$classLabels) $where
            RETURN COUNT(n)""".trimIndent()
        val parameters = mapOf(
            "visibility" to when (visibility) {
                VisibilityFilter.UNLISTED -> Visibility.UNLISTED
                VisibilityFilter.FEATURED -> Visibility.FEATURED
                VisibilityFilter.NON_FEATURED -> Visibility.DEFAULT
                VisibilityFilter.DELETED -> Visibility.DELETED
                else -> null
            }?.name,
            "organizationId" to organizationId?.value?.toString(),
            "observatoryId" to observatoryId?.value?.toString(),
            "createdBy" to createdBy?.value?.toString(),
            "createdAt" to createdAt?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        )
        val elements = neo4jClient.query(query)
            .bindAll(parameters + mapOf("skip" to pageable.offset, "limit" to pageable.pageSize))
            .fetchAs(Resource::class.java)
            .mappedBy(ResourceMapper("n"))
            .all()
        val count = neo4jClient.query(countQuery)
            .bindAll(parameters)
            .fetchAs(Long::class.java)
            .one()
            .orElse(0)
        return PageImpl(elements.toList(), pageable, count)
    }

    @Cacheable(key = "#id", cacheNames = [RESOURCE_ID_TO_RESOURCE_EXISTS_CACHE])
    override fun exists(id: ThingId): Boolean = neo4jRepository.existsById(id)

    @Cacheable(key = "#id", cacheNames = [RESOURCE_ID_TO_RESOURCE_CACHE])
    override fun findById(id: ThingId): Optional<Resource> =
        neo4jRepository.findById(id).map(Neo4jResource::toResource)

    override fun findAllPapersByLabel(label: String): Iterable<Resource> =
        neo4jRepository.findAllPapersByLabel(label).map(Neo4jResource::toResource)

    override fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable): Page<Resource> =
        when (labelSearchString) {
            is ExactSearchString -> neo4jRepository.findAllByLabel(
                query = labelSearchString.query,
                label = labelSearchString.input,
                pageable = pageable
            )
            is FuzzySearchString -> neo4jRepository.findAllByLabelContaining(
                label = labelSearchString.query,
                minLabelLength = labelSearchString.input.length,
                pageable = pageable
            )
        }.map(Neo4jResource::toResource)

    override fun findAllByClass(`class`: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByClass(`class`, pageable).map(Neo4jResource::toResource)

    override fun findAllByClassAndCreatedBy(
        `class`: ThingId,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllByClassAndCreatedBy(`class`, createdBy, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndLabel(`class`: ThingId, labelSearchString: SearchString, pageable: Pageable): Page<Resource> =
        when (labelSearchString) {
            is ExactSearchString -> neo4jRepository.findAllByClassAndLabel(
                `class` = `class`,
                query = labelSearchString.query,
                label = labelSearchString.input,
                pageable = pageable
            )
            is FuzzySearchString -> neo4jRepository.findAllByClassAndLabelContaining(
                `class` = `class`,
                label = labelSearchString.query,
                minLabelLength = labelSearchString.input.length,
                pageable = pageable
            )
        }.map(Neo4jResource::toResource)

    override fun findAllByClassAndLabelAndCreatedBy(
        `class`: ThingId,
        labelSearchString: SearchString,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource> =
        when (labelSearchString) {
            is ExactSearchString -> neo4jRepository.findAllByClassAndLabelAndCreatedBy(
                `class` = `class`,
                query = labelSearchString.query,
                label = labelSearchString.input,
                createdBy = createdBy,
                pageable = pageable
            )
            is FuzzySearchString -> neo4jRepository.findAllByClassAndLabelContainingAndCreatedBy(
                `class` = `class`,
                label = labelSearchString.query,
                createdBy = createdBy,
                minLabelLength = labelSearchString.input.length,
                pageable = pageable
            )
        }.map(Neo4jResource::toResource)

    override fun findAllIncludingAndExcludingClasses(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllIncludingAndExcludingClasses(
            includeClasses,
            excludeClasses,
            pageable
        ).map(Neo4jResource::toResource)

    override fun findAllIncludingAndExcludingClassesByLabel(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        labelSearchString: SearchString,
        pageable: Pageable
    ): Page<Resource> =
        when (labelSearchString) {
            is ExactSearchString -> neo4jRepository.findAllIncludingAndExcludingClassesByLabel(
                includeClasses = includeClasses,
                excludeClasses = excludeClasses,
                query = labelSearchString.query,
                label = labelSearchString.input,
                pageable = pageable
            )
            is FuzzySearchString -> neo4jRepository.findAllIncludingAndExcludingClassesByLabelContaining(
                includeClasses = includeClasses,
                excludeClasses = excludeClasses,
                label = labelSearchString.query,
                minLabelLength = labelSearchString.input.length,
                pageable = pageable
            )
        }.map(Neo4jResource::toResource)

    override fun findPaperByLabel(label: String): Optional<Resource> =
        neo4jRepository.findPaperByLabel(label).map(Neo4jResource::toResource)

    override fun findAllByClassAndObservatoryId(
        `class`: ThingId,
        id: ObservatoryId,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllByClassAndObservatoryId(`class`, id, pageable)
            .map(Neo4jResource::toResource)

    override fun findPaperById(id: ThingId): Optional<Resource> =
        neo4jRepository.findPaperById(id)
            .map(Neo4jResource::toResource)

    override fun findAllPapersByVerified(verified: Boolean, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllPapersByVerified(verified, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByVisibility(visibility, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllListed(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListed(pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByClassAndVisibility(classId: ThingId, visibility: Visibility, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByClassAndVisibility(classId, visibility, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllListedByClass(classId: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedByClass(classId, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByClassInAndVisibility(
        classes: Set<ThingId>,
        visibility: Visibility,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllByClassInAndVisibility(classes, visibility, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllListedByClassIn(classes: Set<ThingId>, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedByClassIn(classes, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllByClassInAndVisibilityAndObservatoryId(
        classes: Set<ThingId>,
        visibility: Visibility,
        id: ObservatoryId,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllByClassInAndVisibilityAndObservatoryId(classes, visibility, id, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllListedByClassInAndObservatoryId(
        classes: Set<ThingId>,
        id: ObservatoryId,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllListedByClassInAndObservatoryId(classes, id, pageable)
            .map(Neo4jResource::toResource)

    override fun findAllContributorIds(pageable: Pageable): Page<ContributorId> =
        neo4jRepository.findAllContributorIds(pageable).map(::ContributorId)

    override fun findAllComparisonsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllComparisonsByOrganizationId(id, pageable).map(Neo4jResource::toResource)

    private fun Resource.toNeo4jResource() =
        // We need to fetch the original resource, so "resources" is set properly.
        neo4jRepository.findById(this.id).orElseGet(::Neo4jResource).apply {
            id = this@toNeo4jResource.id
            label = this@toNeo4jResource.label
            created_by = this@toNeo4jResource.createdBy
            created_at = this@toNeo4jResource.createdAt
            observatory_id = this@toNeo4jResource.observatoryId
            extraction_method = this@toNeo4jResource.extractionMethod
            verified = this@toNeo4jResource.verified
            visibility = this@toNeo4jResource.visibility
            organization_id = this@toNeo4jResource.organizationId
            classes = this@toNeo4jResource.classes
            unlisted_by = this@toNeo4jResource.unlistedBy
        }
}
