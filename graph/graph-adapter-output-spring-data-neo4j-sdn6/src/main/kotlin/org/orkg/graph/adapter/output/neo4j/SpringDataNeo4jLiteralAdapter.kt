package org.orkg.graph.adapter.output.neo4j

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.neo4jdsl.appendOrderByOptimizations
import org.orkg.common.neo4jdsl.sortedWith
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jLiteral
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jLiteralIdGenerator
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jLiteralRepository
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.FuzzySearchString
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.SearchString
import org.orkg.graph.output.LiteralRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

const val LITERAL_ID_TO_LITERAL_CACHE = "literal-id-to-literal"
const val LITERAL_ID_TO_LITERAL_EXISTS_CACHE = "literal-id-to-literal-exists"

@Component
@CacheConfig(cacheNames = [LITERAL_ID_TO_LITERAL_CACHE, LITERAL_ID_TO_LITERAL_EXISTS_CACHE])
class SpringDataNeo4jLiteralAdapter(
    private val neo4jRepository: Neo4jLiteralRepository,
    private val neo4jLiteralIdGenerator: Neo4jLiteralIdGenerator,
    private val neo4jClient: Neo4jClient
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

    override fun findAll(pageable: Pageable): Page<Literal> =
        findAllWithFilters(pageable = pageable)

    override fun findAllWithFilters(
        createdBy: ContributorId?,
        createdAt: OffsetDateTime?,
        pageable: Pageable
    ): Page<Literal> {
        val where = buildString {
            if (createdBy != null) {
                append(" AND n.created_by = ${'$'}createdBy")
            }
            if (createdAt != null) {
                append(" AND n.created_at = ${'$'}createdAt")
            }
            appendOrderByOptimizations(pageable, createdAt, createdBy)
        }.replaceFirst(" AND", "WHERE")
        val query = """
            MATCH (n:Literal) $where
            RETURN n, n.id AS id, n.label AS label, n.created_by AS created_by, n.created_at AS created_at
            SKIP ${'$'}skip LIMIT ${'$'}limit""".sortedWith(pageable.sort).trimIndent()
        val countQuery = """
            MATCH (n:Literal) $where
            RETURN COUNT(n)""".trimIndent()
        val parameters = mapOf(
            "createdBy" to createdBy?.value?.toString(),
            "createdAt" to createdAt?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        )
        val elements = neo4jClient.query(query)
            .bindAll(parameters + mapOf("skip" to pageable.offset, "limit" to pageable.pageSize))
            .fetchAs(Literal::class.java)
            .mappedBy(LiteralMapper("n"))
            .all()
        val count = neo4jClient.query(countQuery)
            .bindAll(parameters)
            .fetchAs(Long::class.java)
            .one()
            .orElse(0)
        return PageImpl(elements.toList(), pageable, count)
    }

    @Cacheable(key = "#id", cacheNames = [LITERAL_ID_TO_LITERAL_CACHE])
    override fun findById(id: ThingId): Optional<Literal> =
        neo4jRepository.findById(id).map(Neo4jLiteral::toLiteral)

    override fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable): Page<Literal> =
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
        }.map(Neo4jLiteral::toLiteral)

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
