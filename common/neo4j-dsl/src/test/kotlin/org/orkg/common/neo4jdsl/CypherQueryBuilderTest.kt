package org.orkg.common.neo4jdsl

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.neo4j.cypherdsl.core.Cypher.anyNode
import org.neo4j.cypherdsl.core.Cypher.match
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Functions.count
import org.neo4j.driver.summary.ResultSummary
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.neo4j.core.Neo4jClient

class CypherQueryBuilderTest {
    private val neo4jClient: Neo4jClient = mockk()
    private val unboundRunnableSpec: Neo4jClient.UnboundRunnableSpec = mockk()
    private val runnableSpec: Neo4jClient.RunnableSpec = mockk()
    private val resultSummary: ResultSummary = mockk()
    private val stringMappingSpec: Neo4jClient.MappingSpec<String> = mockk()
    private val longMappingSpec: Neo4jClient.MappingSpec<Long> = mockk()

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(neo4jClient, unboundRunnableSpec, runnableSpec, resultSummary, stringMappingSpec, longMappingSpec)
    }

    class SimpleQueryCache : QueryCache {
        private val cache: MutableMap<Any, String> = mutableMapOf()

        override fun getOrPut(key: Any, valueSupplier: () -> String): String =
            cache.getOrPut(key, valueSupplier)

        val size: Int get() = cache.size
    }

    @Test
    fun `single query builder makes use of memoization`() {
        val queryCache = SimpleQueryCache()

        every { neo4jClient.query(any<String>()) } returns unboundRunnableSpec
        every { unboundRunnableSpec.bindAll(any()) } returns runnableSpec
        every { runnableSpec.run() } returns resultSummary

        val runQuery = {
            CypherQueryBuilder(neo4jClient, queryCache)
                .withQuery {
                    val node = anyNode().named("node")
                    match(node).returning(count(node))
                }
                .run()
        }

        assertEquals(0, queryCache.size)
        runQuery()
        assertEquals(1, queryCache.size)
        runQuery()
        assertEquals(1, queryCache.size)

        verify(exactly = 2) { neo4jClient.query(any<String>()) }
        verify(exactly = 2) { unboundRunnableSpec.bindAll(any()) }
        verify(exactly = 2) { runnableSpec.run() }
    }

    @Test
    fun `paged query builder makes use of memoization`() {
        val queryCache = SimpleQueryCache()

        every { neo4jClient.query(any<String>()) } returns unboundRunnableSpec
        every { unboundRunnableSpec.bindAll(any()) } returns runnableSpec
        every { runnableSpec.fetchAs(String::class.java) } returns stringMappingSpec
        every { stringMappingSpec.all() } returns listOf("result")
        every { runnableSpec.fetchAs(Long::class.java) } returns longMappingSpec
        every { longMappingSpec.one() } returns Optional.of(1L)

        val runQuery = {
            CypherQueryBuilder(neo4jClient, queryCache)
                .withCommonQuery {
                    match(anyNode().named("node"))
                }
                .withQuery { commonQuery ->
                    commonQuery.returning(name("node").property("label"))
                }
                .withCountQuery { commonQuery ->
                    commonQuery.returning(count(name("node")))
                }
                .fetchAs(String::class)
                .fetch(PageRequest.of(0, 5))
        }

        assertEquals(0, queryCache.size)
        runQuery()
        assertEquals(2, queryCache.size)
        runQuery()
        assertEquals(2, queryCache.size)

        verify(exactly = 4) { neo4jClient.query(any<String>()) }
        verify(exactly = 4) { unboundRunnableSpec.bindAll(any()) }
        verify(exactly = 2) { runnableSpec.fetchAs(String::class.java) }
        verify(exactly = 2) { stringMappingSpec.all() }
        verify(exactly = 2) { runnableSpec.fetchAs(Long::class.java) }
        verify(exactly = 2) { longMappingSpec.one() }
    }

    @Test
    fun `paged query builder does not add order by clause when unsorted`() {
        every { neo4jClient.query(any<String>()) } returns unboundRunnableSpec
        every { unboundRunnableSpec.bindAll(any()) } returns runnableSpec
        every { runnableSpec.fetchAs(String::class.java) } returns stringMappingSpec
        every { stringMappingSpec.all() } returns listOf("result")
        every { runnableSpec.fetchAs(Long::class.java) } returns longMappingSpec
        every { longMappingSpec.one() } returns Optional.of(1L)

        CypherQueryBuilder(neo4jClient)
            .withCommonQuery {
                match(anyNode().named("node"))
            }
            .withQuery { commonQuery ->
                commonQuery.returning(name("node").property("label"))
            }
            .withCountQuery { commonQuery ->
                commonQuery.returning(count(name("node")))
            }
            .fetchAs(String::class)
            .fetch(PageRequest.of(0, 5))

        verify(exactly = 1) { neo4jClient.query("MATCH (node) RETURN node.label SKIP \$sdnSkip LIMIT \$sdnLimit") }
        verify(exactly = 1) { neo4jClient.query("MATCH (node) RETURN count(node)") }
        verify(exactly = 2) { unboundRunnableSpec.bindAll(any()) }
        verify(exactly = 1) { runnableSpec.fetchAs(String::class.java) }
        verify(exactly = 1) { stringMappingSpec.all() }
        verify(exactly = 1) { runnableSpec.fetchAs(Long::class.java) }
        verify(exactly = 1) { longMappingSpec.one() }
    }

    @Test
    fun `paged query builder generates order by clause correctly`() {
        every { neo4jClient.query(any<String>()) } returns unboundRunnableSpec
        every { unboundRunnableSpec.bindAll(any()) } returns runnableSpec
        every { runnableSpec.fetchAs(String::class.java) } returns stringMappingSpec
        every { stringMappingSpec.all() } returns listOf("result")
        every { runnableSpec.fetchAs(Long::class.java) } returns longMappingSpec
        every { longMappingSpec.one() } returns Optional.of(1L)

        val sort = Sort.by("property1").ascending().and(Sort.by("property2").descending())

        CypherQueryBuilder(neo4jClient)
            .withCommonQuery {
                match(anyNode().named("node"))
            }
            .withQuery { commonQuery ->
                commonQuery.returning(name("node").property("label"))
            }
            .withCountQuery { commonQuery ->
                commonQuery.returning(count(name("node")))
            }
            .fetchAs(String::class)
            .fetch(PageRequest.of(0, 5, sort))

        verify(exactly = 1) { neo4jClient.query("MATCH (node) RETURN node.label ORDER BY property1 ASC, property2 DESC SKIP \$sdnSkip LIMIT \$sdnLimit") }
        verify(exactly = 1) { neo4jClient.query("MATCH (node) RETURN count(node)") }
        verify(exactly = 2) { unboundRunnableSpec.bindAll(any()) }
        verify(exactly = 1) { runnableSpec.fetchAs(String::class.java) }
        verify(exactly = 1) { stringMappingSpec.all() }
        verify(exactly = 1) { runnableSpec.fetchAs(Long::class.java) }
        verify(exactly = 1) { longMappingSpec.one() }
    }
}
