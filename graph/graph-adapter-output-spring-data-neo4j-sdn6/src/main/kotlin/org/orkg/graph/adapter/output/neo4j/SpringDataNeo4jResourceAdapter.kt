package org.orkg.graph.adapter.output.neo4j

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.*
import org.neo4j.cypherdsl.core.Condition
import org.neo4j.cypherdsl.core.Conditions
import org.neo4j.cypherdsl.core.Cypher.anonParameter
import org.neo4j.cypherdsl.core.Cypher.call
import org.neo4j.cypherdsl.core.Cypher.literalOf
import org.neo4j.cypherdsl.core.Cypher.match
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.node
import org.neo4j.cypherdsl.core.Functions.labels
import org.neo4j.cypherdsl.core.Functions.size
import org.neo4j.cypherdsl.core.Functions.toLower
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.neo4jdsl.CypherQueryBuilder
import org.orkg.common.neo4jdsl.PagedQueryBuilder.countOver
import org.orkg.common.neo4jdsl.PagedQueryBuilder.mappedBy
import org.orkg.common.neo4jdsl.QueryCache
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jResource
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jResourceIdGenerator
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jResourceRepository
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.FuzzySearchString
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.output.ResourceRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

const val RESOURCE_ID_TO_RESOURCE_CACHE = "resource-id-to-resource"
const val RESOURCE_ID_TO_RESOURCE_EXISTS_CACHE = "resource-id-to-resource-exists"
private const val FULLTEXT_INDEX_FOR_LABEL = "fulltext_idx_for_resource_on_label"

@Component
@CacheConfig(cacheNames = [RESOURCE_ID_TO_RESOURCE_CACHE, RESOURCE_ID_TO_RESOURCE_EXISTS_CACHE])
class SpringDataNeo4jResourceAdapter(
    private val neo4jRepository: Neo4jResourceRepository,
    private val neo4jResourceIdGenerator: Neo4jResourceIdGenerator,
    private val neo4jClient: Neo4jClient
) : ResourceRepository {
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
        findAll(
            pageable = pageable,
            label = null,
            visibility = null,
            createdBy = null,
            createdAtStart = null,
            createdAtEnd = null,
            includeClasses = emptySet(),
            excludeClasses = emptySet(),
            observatoryId = null,
            organizationId = null
        )

    @Cacheable(key = "#id", cacheNames = [RESOURCE_ID_TO_RESOURCE_EXISTS_CACHE])
    override fun exists(id: ThingId): Boolean = neo4jRepository.existsById(id)

    @Cacheable(key = "#id", cacheNames = [RESOURCE_ID_TO_RESOURCE_CACHE])
    override fun findById(id: ThingId): Optional<Resource> =
        neo4jRepository.findById(id).map(Neo4jResource::toResource)

    override fun findAll(
        pageable: Pageable,
        label: SearchString?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?
    ): Page<Resource> = CypherQueryBuilder(neo4jClient, QueryCache.Uncached)
        .withCommonQuery {
            val node = node("Resource", includeClasses.map { it.value }).named("node")
            val match = label?.let { searchString ->
                val labelCondition = includeClasses.toCondition {
                    anonParameter(includeClasses.map { it.value }).includesAll(labels(node))
                }
                when (searchString) {
                    is ExactSearchString -> {
                        call("db.index.fulltext.queryNodes")
                            .withArgs(anonParameter(FULLTEXT_INDEX_FOR_LABEL), anonParameter(searchString.query))
                            .yield("node")
                            .where(toLower(node.property("label")).eq(toLower(anonParameter(searchString.input))).and(labelCondition))
                            .with(node)
                    }
                    is FuzzySearchString -> {
                        call("db.index.fulltext.queryNodes")
                            .withArgs(anonParameter(FULLTEXT_INDEX_FOR_LABEL), anonParameter(searchString.query))
                            .yield("node", "score")
                            .where(size(node.property("label")).gte(anonParameter(searchString.input.length)).and(labelCondition))
                            .with(node, name("score"))
                    }
                }
            } ?: match(node).with(node)
            match.where(
                visibility.toCondition { filter ->
                    filter.targets.map { node.property("visibility").eq(literalOf<String>(it.name)) }
                        .reduceOrNull(Condition::or) ?: Conditions.noCondition()
                },
                createdBy.toCondition { node.property("created_by").eq(anonParameter(it.value.toString())) },
                createdAtStart.toCondition { node.property("created_at").gte(anonParameter(it.format(ISO_OFFSET_DATE_TIME))) },
                createdAtEnd.toCondition { node.property("created_at").lte(anonParameter(it.format(ISO_OFFSET_DATE_TIME))) },
                excludeClasses.toCondition { labels(node).includesAny(anonParameter(it.map { id -> id.value })).not() },
                observatoryId.toCondition { node.property("observatory_id").eq(anonParameter(it.value.toString())) },
                organizationId.toCondition { node.property("organization_id").eq(anonParameter(it.value.toString())) }
            )
        }
        .withQuery { commonQuery ->
            val node = name("node")
            val score = if (label != null && label is FuzzySearchString) name("score") else null
            val variables = listOfNotNull(node, score)
            val pageableWithDefaultSort = pageable.withDefaultSort { Sort.by("created_at") }
            commonQuery
                .with(variables) // "with" is required because cypher dsl reorders "orderBy" and "where" clauses sometimes, decreasing performance
                .where(
                    orderByOptimizations(
                        node = node,
                        sort = pageableWithDefaultSort.sort,
                        properties = arrayOf("id", "label", "created_at", "created_by", "visibility")
                    )
                )
                .with(variables)
                .orderBy(
                    if (score != null) {
                        listOf(
                            size(node.property("label")).ascending(),
                            score.descending(),
                            node.property("created_at").ascending()
                        )
                    } else {
                        pageableWithDefaultSort.sort.toSortItems(
                            node = node,
                            knownProperties = arrayOf("id", "label", "created_at", "created_by", "visibility")
                        )
                    }
                )
                .returning(node)
        }
        .countOver("node")
        .mappedBy(ResourceMapper("node"))
        .fetch(pageable, false)

    override fun findAllPapersByLabel(label: String): Iterable<Resource> =
        neo4jRepository.findAllPapersByLabel(label).map(Neo4jResource::toResource)

    override fun findPaperByLabel(label: String): Optional<Resource> =
        neo4jRepository.findPaperByLabel(label).map(Neo4jResource::toResource)

    override fun findAllPapersByVerified(verified: Boolean, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllPapersByVerified(verified, pageable)
            .map(Neo4jResource::toResource)

    override fun findPaperById(id: ThingId): Optional<Resource> =
        neo4jRepository.findPaperById(id)
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
