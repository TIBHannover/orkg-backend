package org.orkg.graph.adapter.output.neo4j

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.neo4jdsl.appendOrderByOptimizations
import org.orkg.common.neo4jdsl.sortedWith
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jPredicate
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jPredicateIdGenerator
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jPredicateRepository
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.FuzzySearchString
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.SearchString
import org.orkg.graph.output.PredicateRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

const val PREDICATE_ID_TO_PREDICATE_CACHE = "predicate-id-to-predicate"

@Component
@CacheConfig(cacheNames = [PREDICATE_ID_TO_PREDICATE_CACHE])
class SpringDataNeo4jPredicateAdapter(
    private val neo4jRepository: Neo4jPredicateRepository,
    private val idGenerator: Neo4jPredicateIdGenerator,
    private val neo4jClient: Neo4jClient
) : PredicateRepository {
    override fun findAll(pageable: Pageable): Page<Predicate> =
        findAllWithFilters(pageable = pageable)

    override fun exists(id: ThingId): Boolean = neo4jRepository.existsById(id)

    override fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable): Page<Predicate> =
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
        }.map(Neo4jPredicate::toPredicate)

    @Cacheable(key = "#id")
    override fun findById(id: ThingId): Optional<Predicate> =
        neo4jRepository.findById(id).map(Neo4jPredicate::toPredicate)

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

    @Caching(
        evict = [
            CacheEvict(key = "#predicate.id"),
            CacheEvict(key = "#predicate.id", cacheNames = [THING_ID_TO_THING_CACHE]),
        ]
    )
    override fun save(predicate: Predicate) {
        neo4jRepository.save(predicate.toNeo4jPredicate())
    }

    override fun nextIdentity(): ThingId {
        // IDs could exist already by manual creation. We need to find the next available one.
        var id: ThingId
        do {
            id = idGenerator.nextIdentity()
        } while (neo4jRepository.existsById(id))
        return id
    }

    override fun findAllWithFilters(
        createdBy: ContributorId?,
        createdAt: OffsetDateTime?,
        pageable: Pageable
    ): Page<Predicate> {
        val where = buildString {
            if (createdBy != null) {
                append(" AND n.created_by = ${'$'}createdBy")
            }
            if (createdAt != null) {
                append(" AND n.created_at = ${'$'}createdAt")
            }
            appendOrderByOptimizations(pageable, createdAt, createdBy?.value)
        }.replaceFirst(" AND", "WHERE")
        val query = """
            MATCH (n:Predicate) $where
            RETURN n, n.id AS id, n.label AS label, n.created_by AS created_by, n.created_at AS created_at
            SKIP ${'$'}skip LIMIT ${'$'}limit""".sortedWith(pageable.sort).trimIndent()
        val countQuery = """
            MATCH (n:Predicate) $where
            RETURN COUNT(n)""".trimIndent()
        val parameters = mapOf(
            "createdBy" to createdBy?.value?.toString(),
            "createdAt" to createdAt?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        )
        val elements = neo4jClient.query(query)
            .bindAll(parameters + mapOf("skip" to pageable.offset, "limit" to pageable.pageSize))
            .fetchAs(Predicate::class.java)
            .mappedBy(PredicateMapper("n"))
            .all()
        val count = neo4jClient.query(countQuery)
            .bindAll(parameters)
            .fetchAs(Long::class.java)
            .one()
            .orElse(0)
        return PageImpl(elements.toList(), pageable, count)
    }

    private fun Predicate.toNeo4jPredicate() =
        neo4jRepository.findById(this.id).orElseGet(::Neo4jPredicate).apply {
            id = this@toNeo4jPredicate.id
            label = this@toNeo4jPredicate.label
            created_by = this@toNeo4jPredicate.createdBy
            created_at = this@toNeo4jPredicate.createdAt
            modifiable = this@toNeo4jPredicate.modifiable
        }
}
