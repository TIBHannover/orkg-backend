package org.orkg.graph.adapter.output.neo4j

import org.neo4j.cypherdsl.core.Cypher
import org.neo4j.cypherdsl.core.Cypher.anonParameter
import org.neo4j.cypherdsl.core.Cypher.call
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.size
import org.neo4j.cypherdsl.core.Cypher.toLower
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.neo4jdsl.CypherQueryBuilderFactory
import org.orkg.common.neo4jdsl.PagedQueryBuilder.countOver
import org.orkg.common.neo4jdsl.PagedQueryBuilder.mappedBy
import org.orkg.common.neo4jdsl.QueryCache.Uncached
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jPredicate
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jPredicateIdGenerator
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jPredicateRepository
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.FuzzySearchString
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.SearchString
import org.orkg.graph.output.PredicateRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.Optional

const val PREDICATE_ID_TO_PREDICATE_CACHE = "predicate-id-to-predicate"

private const val FULLTEXT_INDEX_FOR_LABEL = "fulltext_idx_for_predicate_on_label"

@Component
@TransactionalOnNeo4j
@CacheConfig(cacheNames = [PREDICATE_ID_TO_PREDICATE_CACHE])
class SpringDataNeo4jPredicateAdapter(
    private val neo4jRepository: Neo4jPredicateRepository,
    private val idGenerator: Neo4jPredicateIdGenerator,
    private val cypherQueryBuilderFactory: CypherQueryBuilderFactory,
) : PredicateRepository {
    override fun existsById(id: ThingId): Boolean = neo4jRepository.existsById(id)

    @Cacheable(key = "#id")
    override fun findById(id: ThingId): Optional<Predicate> =
        neo4jRepository.findById(id).map(Neo4jPredicate::toPredicate)

    override fun findAll(pageable: Pageable): Page<Predicate> =
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
        createdAtEnd: OffsetDateTime?,
    ): Page<Predicate> = cypherQueryBuilderFactory.newBuilder(Uncached)
        .withCommonQuery {
            val node = Cypher.node("Predicate").named("node")
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
        .mappedBy(PredicateMapper("node"))
        .fetch(pageable, false)

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

    override fun isInUse(id: ThingId): Boolean =
        neo4jRepository.isInUse(id)

    private fun Predicate.toNeo4jPredicate() =
        neo4jRepository.findById(this.id).orElseGet(::Neo4jPredicate).apply {
            id = this@toNeo4jPredicate.id
            label = this@toNeo4jPredicate.label
            created_by = this@toNeo4jPredicate.createdBy
            created_at = this@toNeo4jPredicate.createdAt
            modifiable = this@toNeo4jPredicate.modifiable
        }
}
