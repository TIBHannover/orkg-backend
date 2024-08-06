package org.orkg.graph.adapter.output.neo4j

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.*
import org.neo4j.cypherdsl.core.Cypher
import org.neo4j.cypherdsl.core.Cypher.anonParameter
import org.neo4j.cypherdsl.core.Cypher.call
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Functions.size
import org.neo4j.cypherdsl.core.Functions.toLower
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.neo4jdsl.CypherQueryBuilder
import org.orkg.common.neo4jdsl.PagedQueryBuilder.countOver
import org.orkg.common.neo4jdsl.PagedQueryBuilder.mappedBy
import org.orkg.common.neo4jdsl.QueryCache.Uncached
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jClass
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jClassIdGenerator
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jClassRepository
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.FuzzySearchString
import org.orkg.graph.domain.SearchString
import org.orkg.graph.output.ClassRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

const val CLASS_ID_TO_CLASS_CACHE = "class-id-to-class"
const val CLASS_ID_TO_CLASS_EXISTS_CACHE = "class-id-to-class-exists"

private const val FULLTEXT_INDEX_FOR_LABEL = "fulltext_idx_for_class_on_label"

@Component
@CacheConfig(cacheNames = [CLASS_ID_TO_CLASS_CACHE, CLASS_ID_TO_CLASS_EXISTS_CACHE])
class SpringDataNeo4jClassAdapter(
    private val neo4jRepository: Neo4jClassRepository,
    private val neo4jClassIdGenerator: Neo4jClassIdGenerator,
    private val neo4jClient: Neo4jClient
) : ClassRepository {
    @Caching(
        evict = [
            CacheEvict(key = "#c.id", cacheNames = [CLASS_ID_TO_CLASS_CACHE]),
            CacheEvict(key = "#c.id", cacheNames = [THING_ID_TO_THING_CACHE]),
        ]
    )
    override fun save(c: Class) {
        neo4jRepository.save(c.toNeo4jClass())
    }

    override fun findAll(pageable: Pageable): Page<Class> =
        findAll(
            pageable = pageable,
            label = null,
            createdBy = null,
            createdAtStart = null,
            createdAtEnd = null
        )

    override fun findAll(
        pageable: Pageable,
        label: SearchString?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?
    ): Page<Class> = CypherQueryBuilder(neo4jClient, Uncached)
        .withCommonQuery {
            val node = Cypher.node("Class").named("node")
            val match = label?.let { searchString ->
                when (searchString) {
                    is ExactSearchString -> {
                        call("db.index.fulltext.queryNodes")
                            .withArgs(anonParameter(FULLTEXT_INDEX_FOR_LABEL), anonParameter(searchString.query))
                            .yield("node")
                            .where(toLower(node.property("label")).eq(toLower(anonParameter(searchString.input))))
                            .with(node)
                    }
                    is FuzzySearchString -> {
                        call("db.index.fulltext.queryNodes")
                            .withArgs(anonParameter(FULLTEXT_INDEX_FOR_LABEL), anonParameter(searchString.query))
                            .yield("node", "score")
                            .where(size(node.property("label")).gte(anonParameter(searchString.input.length)))
                            .with(node, name("score"))
                    }
                }
            } ?: Cypher.match(node).with(node)
            match.where(
                createdBy.toCondition { node.property("created_by").eq(anonParameter(it.value.toString())) },
                createdAtStart.toCondition { node.property("created_at").gte(anonParameter(it.format(ISO_OFFSET_DATE_TIME))) },
                createdAtEnd.toCondition { node.property("created_at").lte(anonParameter(it.format(ISO_OFFSET_DATE_TIME))) }
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
                        properties = arrayOf("id", "label", "created_at", "created_by")
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
                            knownProperties = arrayOf("id", "label", "created_at", "created_by")
                        )
                    }
                )
                .returning(node)
        }
        .countOver("node")
        .mappedBy(ClassMapper("node"))
        .fetch(pageable, false)

    @Cacheable(key = "#id", cacheNames = [CLASS_ID_TO_CLASS_EXISTS_CACHE])
    override fun exists(id: ThingId): Boolean = neo4jRepository.existsById(id)

    override fun existsAll(ids: Set<ThingId>): Boolean = neo4jRepository.existsAllById(ids)

    @Cacheable(key = "#id", cacheNames = [CLASS_ID_TO_CLASS_CACHE])
    override fun findById(id: ThingId): Optional<Class> =
        neo4jRepository.findById(id).map(Neo4jClass::toClass)

    @Deprecated("For removal")
    override fun findAllById(id: Iterable<ThingId>, pageable: Pageable): Page<Class> =
        neo4jRepository.findAllByIdIn(id, pageable).map(Neo4jClass::toClass)

    override fun findByUri(uri: String): Optional<Class> = neo4jRepository.findByUri(uri).map(Neo4jClass::toClass)

    @Caching(
        evict = [
            CacheEvict(allEntries = true),
            CacheEvict(allEntries = true, cacheNames = [THING_ID_TO_THING_CACHE]),
        ]
    )
    override fun deleteAll() {
        neo4jRepository.deleteAll()
    }

    override fun nextIdentity(): ThingId {
        // IDs could exist already by manual creation. We need to find the next available one.
        var id: ThingId
        do {
            id = neo4jClassIdGenerator.nextIdentity()
        } while (neo4jRepository.existsById(id))
        return id
    }

    private fun Class.toNeo4jClass(): Neo4jClass =
        neo4jRepository.findById(this.id).orElseGet(::Neo4jClass).apply {
            id = this@toNeo4jClass.id
            label = this@toNeo4jClass.label
            uri = this@toNeo4jClass.uri?.toString()
            created_by = this@toNeo4jClass.createdBy
            created_at = this@toNeo4jClass.createdAt
            modifiable = this@toNeo4jClass.modifiable
        }
}
