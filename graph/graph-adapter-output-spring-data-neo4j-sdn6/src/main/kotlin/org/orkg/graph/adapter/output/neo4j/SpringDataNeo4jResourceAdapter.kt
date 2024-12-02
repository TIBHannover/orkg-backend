package org.orkg.graph.adapter.output.neo4j

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.*
import org.neo4j.cypherdsl.core.Condition
import org.neo4j.cypherdsl.core.Cypher.anonParameter
import org.neo4j.cypherdsl.core.Cypher.call
import org.neo4j.cypherdsl.core.Cypher.coalesce
import org.neo4j.cypherdsl.core.Cypher.collect
import org.neo4j.cypherdsl.core.Cypher.count
import org.neo4j.cypherdsl.core.Cypher.literalOf
import org.neo4j.cypherdsl.core.Cypher.match
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.noCondition
import org.neo4j.cypherdsl.core.Cypher.node
import org.neo4j.cypherdsl.core.Cypher.optionalMatch
import org.neo4j.cypherdsl.core.Cypher.parameter
import org.neo4j.cypherdsl.core.Cypher.size
import org.neo4j.cypherdsl.core.Cypher.toLower
import org.neo4j.cypherdsl.core.Cypher.union
import org.neo4j.cypherdsl.core.Cypher.unwind
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.neo4jdsl.CypherQueryBuilder
import org.orkg.common.neo4jdsl.PagedQueryBuilder.countOver
import org.orkg.common.neo4jdsl.PagedQueryBuilder.mappedBy
import org.orkg.common.neo4jdsl.QueryCache.Uncached
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
private const val INSTANCE_OF = "INSTANCE_OF"
private const val SUBCLASS_OF = "SUBCLASS_OF"

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
        val hlp = name("hlp")
        val neo4jResource = node("Resource", "Thing").named("neo4jResource")
        val label = name("label")
        val c = node("Class").withProperties("id", label).named("c")
        val e = node("Class").named("e")
        val classIds = name("classIds")
        val nextVersion = name("v")
        val created = name("rc")
        val deleted = name("rd")
        val oldResource = neo4jRepository.findById(resource.id)
        val oldLabels = oldResource.map { it.classes }.orElseGet { emptySet() }
        val labelsToAdd = (resource.classes - oldLabels).map(ThingId::value)
        val labelsToRemove = (oldLabels - resource.classes).map(ThingId::value)
        val query = union(
            optionalMatch(node("Resource", "Thing").named(hlp))
                .where(hlp.property("id").eq(parameter("__id__")))
                .with(hlp)
                .where(hlp.isNull)
                .create(neo4jResource.withProperties("version", literalOf<Long>(0L)))
                .with(neo4jResource)
                .mutate(neo4jResource, parameter("__properties__"))
                // cypher dsl does not accept empty label lists
                .let { if (resource.classes.isNotEmpty()) it.set(neo4jResource, resource.classes.map(ThingId::value)) else it }
                .let { if (labelsToRemove.isNotEmpty()) it.remove(neo4jResource, labelsToRemove) else it }
                .with(neo4jResource)
                .call(
                    unwind(parameter("__labels__")).`as`(label)
                        .optionalMatch(c)
                        .with(neo4jResource, c)
                        .where(c.isNotNull) // filter for non-null here because there are no classes available in the repository contract test
                        .create(neo4jResource.relationshipTo(c, INSTANCE_OF).named(created))
                        .returning(count(neo4jResource).`as`("add_instance_of_create_subquery")) // always return a value, so the outer query continues execution
                        .build(),
                    neo4jResource
                )
                .returning(neo4jResource)
                .build(),
            match(neo4jResource)
                .where(
                    neo4jResource.property("id").eq(parameter("__id__"))
                        .and(neo4jResource.property("version").eq(parameter("__version__")))
                )
                // we need to compute the next version here, because cypher dsl throws an error if we try to compute it within the SET clause
                .with(neo4jResource, neo4jResource.property("version").add(literalOf<Long>(1L)).`as`(nextVersion))
                .set(neo4jResource.property("version"), nextVersion)
                .with(neo4jResource)
                .where(
                    neo4jResource.property("version").eq(coalesce(parameter("__version__"), literalOf<Long>(0L)).add(literalOf<Long>(1L)))
                )
                .mutate(neo4jResource, parameter("__properties__"))
                // cypher dsl does not accept empty label lists
                .let { if (resource.classes.isNotEmpty()) it.set(neo4jResource, resource.classes.map(ThingId::value)) else it }
                .let { if (labelsToRemove.isNotEmpty()) it.remove(neo4jResource, labelsToRemove) else it }
                .with(neo4jResource)
                .optionalMatch(neo4jResource.relationshipTo(e, INSTANCE_OF))
                .with(neo4jResource, collect(e.property("id")).`as`(classIds))
                .call(
                    unwind(parameter("__labels_to_add__")).`as`(label)
                        .optionalMatch(c)
                        .with(neo4jResource, c)
                        .where(c.isNotNull) // filter for non-null here because there are no classes available in the repository contract test
                        .create(neo4jResource.relationshipTo(c, INSTANCE_OF).named(created))
                        .returning(count(neo4jResource).`as`("add_instance_of_update_subquery")) // always return a value, so the outer query continues execution
                        .build(),
                    neo4jResource, classIds
                )
                .call(
                    unwind(parameter("__labels_to_remove__")).`as`(label)
                        .match(neo4jResource.relationshipTo(c, INSTANCE_OF).named(deleted))
                        .with(neo4jResource, deleted)
                        .where(c.isNotNull)
                        .delete(deleted)
                        .returning(count(neo4jResource).`as`("remove_instance_of_update_subquery")) // always return a value, so the outer query continues execution
                        .build(),
                    neo4jResource, classIds
                )
                .returning(neo4jResource)
                .build()
        )
        neo4jClient.query(query.cypher)
            .bindAll(
                mapOf(
                    "__id__" to resource.id.value,
                    "__labels__" to resource.classes.map { it.value },
                    "__labels_to_add__" to labelsToAdd,
                    "__labels_to_remove__" to labelsToRemove,
                    "__version__" to oldResource.map { @Suppress("DEPRECATION") it.version }.orElse(1L),
                    "__properties__" to mapOf(
                        "id" to resource.id.value,
                        "label" to resource.label,
                        "created_by" to resource.createdBy.value.toString(),
                        "created_at" to resource.createdAt.format(ISO_OFFSET_DATE_TIME),
                        "observatory_id" to resource.observatoryId.value.toString(),
                        "organization_id" to resource.organizationId.value.toString(),
                        "extraction_method" to resource.extractionMethod.name,
                        "verified" to resource.verified,
                        "visibility" to resource.visibility.name,
                        "unlisted_by" to resource.unlistedBy?.value?.toString(),
                        "modifiable" to resource.modifiable
                    )
                )
            )
            .run()
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
        baseClass: ThingId?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?
    ): Page<Resource> = CypherQueryBuilder(neo4jClient, Uncached)
        .withCommonQuery {
            val node = node("Resource", includeClasses.map { it.value }).named("node")
            val match = label?.let { searchString ->
                val labelCondition = includeClasses.toCondition {
                    node.hasLabels(*includeClasses.map { it.value }.toTypedArray())
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
                        .reduceOrNull(Condition::or) ?: noCondition()
                },
                createdBy.toCondition { node.property("created_by").eq(anonParameter(it.value.toString())) },
                createdAtStart.toCondition { node.property("created_at").gte(anonParameter(it.format(ISO_OFFSET_DATE_TIME))) },
                createdAtEnd.toCondition { node.property("created_at").lte(anonParameter(it.format(ISO_OFFSET_DATE_TIME))) },
                excludeClasses.toCondition { classes -> classes.map { node.hasLabels(it.value).not() }.reduce(Condition::and) },
                observatoryId.toCondition { node.property("observatory_id").eq(anonParameter(it.value.toString())) },
                organizationId.toCondition { node.property("organization_id").eq(anonParameter(it.value.toString())) },
                baseClass.toCondition {
                    node.relationshipTo(node("Class"), INSTANCE_OF)
                        .relationshipTo(node("Class").withProperties("id", anonParameter(it.value)), SUBCLASS_OF)
                        .length(0, null)
                        .asCondition()
                }
            )
        }
        .withQuery { commonQuery ->
            val node = name("node")
            val score = if (label != null && label is FuzzySearchString) name("score") else null
            val variables = listOfNotNull(node, score)
            val sort = pageable.sort.orElseGet { Sort.by("created_at") }
            commonQuery
                .with(variables) // "with" is required because cypher dsl reorders "orderBy" and "where" clauses sometimes, decreasing performance
                .where(
                    orderByOptimizations(
                        node = node,
                        sort = sort,
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
                        sort.toSortItems(
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

    override fun findAllPapersByLabel(label: String): List<Resource> =
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
}
