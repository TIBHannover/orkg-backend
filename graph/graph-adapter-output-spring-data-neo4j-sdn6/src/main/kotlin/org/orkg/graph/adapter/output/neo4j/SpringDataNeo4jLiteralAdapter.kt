package org.orkg.graph.adapter.output.neo4j

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.*
import org.neo4j.cypherdsl.core.Cypher
import org.neo4j.cypherdsl.core.Cypher.anonParameter
import org.neo4j.cypherdsl.core.Cypher.call
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.size
import org.neo4j.cypherdsl.core.Cypher.toLower
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.neo4jdsl.CypherQueryBuilder
import org.orkg.common.neo4jdsl.PagedQueryBuilder.countOver
import org.orkg.common.neo4jdsl.PagedQueryBuilder.mappedBy
import org.orkg.common.neo4jdsl.QueryCache.Uncached
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jLiteral
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jLiteralIdGenerator
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jLiteralRepository
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.FuzzySearchString
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.SearchString
import org.orkg.graph.output.LiteralRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

const val LITERAL_ID_TO_LITERAL_CACHE = "literal-id-to-literal"
const val LITERAL_ID_TO_LITERAL_EXISTS_CACHE = "literal-id-to-literal-exists"

private const val FULLTEXT_INDEX_FOR_LABEL = "fulltext_idx_for_literal_on_label"

@Component
@TransactionalOnNeo4j
@CacheConfig(cacheNames = [LITERAL_ID_TO_LITERAL_CACHE, LITERAL_ID_TO_LITERAL_EXISTS_CACHE])
class SpringDataNeo4jLiteralAdapter(
    private val neo4jRepository: Neo4jLiteralRepository,
    private val neo4jLiteralIdGenerator: Neo4jLiteralIdGenerator,
    private val neo4jClient: Neo4jClient,
) : LiteralRepository {
    override fun nextIdentity(): ThingId {
        // IDs could exist already by manual creation. We need to find the next available one.
        var id: ThingId
        do {
            id = neo4jLiteralIdGenerator.nextIdentity()
        } while (neo4jRepository.existsById(id))
        return id
    }

    @Caching(
        evict = [
            CacheEvict(key = "#literal.id", cacheNames = [LITERAL_ID_TO_LITERAL_CACHE]),
            CacheEvict(key = "#literal.id", cacheNames = [THING_ID_TO_THING_CACHE]),
        ]
    )
    override fun save(literal: Literal) {
        neo4jRepository.save(literal.toNeo4jLiteral())
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

    override fun findAll(
        pageable: Pageable,
        label: SearchString?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?
    ): Page<Literal> = CypherQueryBuilder(neo4jClient, Uncached)
        .withCommonQuery {
            val node = Cypher.node("Literal").named("node")
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
                createdAtEnd.toCondition { node.property("created_at").lte(anonParameter(it.format(ISO_OFFSET_DATE_TIME))) },
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
        .mappedBy(LiteralMapper("node"))
        .fetch(pageable, false)

    @Cacheable(key = "#id", cacheNames = [LITERAL_ID_TO_LITERAL_CACHE])
    override fun findById(id: ThingId): Optional<Literal> =
        neo4jRepository.findById(id).map(Neo4jLiteral::toLiteral)

    @Cacheable(key = "#id", cacheNames = [LITERAL_ID_TO_LITERAL_EXISTS_CACHE])
    override fun exists(id: ThingId): Boolean = neo4jRepository.existsById(id)

    private fun Literal.toNeo4jLiteral() =
        neo4jRepository.findById(this.id).orElseGet(::Neo4jLiteral).apply {
            id = this@toNeo4jLiteral.id
            label = this@toNeo4jLiteral.label
            datatype = this@toNeo4jLiteral.datatype
            created_at = this@toNeo4jLiteral.createdAt
            created_by = this@toNeo4jLiteral.createdBy
            modifiable = this@toNeo4jLiteral.modifiable
        }
}
