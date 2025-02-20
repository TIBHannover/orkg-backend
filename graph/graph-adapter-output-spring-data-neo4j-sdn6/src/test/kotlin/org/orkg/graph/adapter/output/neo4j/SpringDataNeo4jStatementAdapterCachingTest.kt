package org.orkg.graph.adapter.output.neo4j

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.neo4j.driver.Record
import org.neo4j.driver.types.TypeSystem
import org.orkg.common.ThingId
import org.orkg.common.neo4jdsl.CypherQueryBuilderFactory
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jLiteralRepository
import org.orkg.graph.domain.StatementId
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createNeo4jLiteral
import org.orkg.graph.testing.fixtures.createStatement
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.Neo4jClient.UnboundRunnableSpec
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.util.AopTestUtils
import java.util.Optional
import java.util.function.BiFunction
import org.neo4j.cypherdsl.core.renderer.Configuration as CypherConfiguration

private val allCacheNames: Array<out String> = arrayOf(
    THING_ID_TO_THING_CACHE,
    CLASS_ID_TO_CLASS_CACHE,
    CLASS_ID_TO_CLASS_EXISTS_CACHE,
    LITERAL_ID_TO_LITERAL_CACHE,
    LITERAL_ID_TO_LITERAL_EXISTS_CACHE,
    PREDICATE_ID_TO_PREDICATE_CACHE,
    RESOURCE_ID_TO_RESOURCE_CACHE,
    RESOURCE_ID_TO_RESOURCE_EXISTS_CACHE,
)

@ContextConfiguration
@ExtendWith(SpringExtension::class)
internal class SpringDataNeo4jStatementAdapterCachingTest : MockkBaseTest {
    // Autowire the beans created by the configuration below.
    // The cacheManager just needs to be present, and we will use an in-memory one here.
    // The adapters will be wrapped by the caching logic created by Spring.
    // The repositories are proxied objects as well, so we need to get the mocks "out" later.

    @Autowired
    private lateinit var cacheManager: CacheManager

    @Autowired
    private lateinit var statementAdapter: StatementRepository

    @Autowired
    private lateinit var literalAdapter: LiteralRepository

    @Autowired
    private lateinit var neo4jClient: Neo4jClient

    @Autowired
    private lateinit var neo4jLiteralRepository: Neo4jLiteralRepository

    // These references will hold references to the mocks that are wrapped with the caching logic:

    private lateinit var mockedNeo4jLiteralRepository: Neo4jLiteralRepository

    @BeforeEach
    fun resetState() {
        allCacheNames.forEach { name ->
            // Reset the cache. Throw NPE if we cannot find the cache, most likely because the name is wrong.
            cacheManager.getCache(name)!!.clear()
        }

        // Obtain access to the proxied object, which is our mock created in the configuration below.
        mockedNeo4jLiteralRepository = AopTestUtils.getTargetObject(neo4jLiteralRepository)
    }

    @Test
    fun `deleting a statement with a literal object should evict the literal from the literal cache`() {
        val literalId = ThingId("L1")
        val neo4jLiteral = createNeo4jLiteral(id = literalId)
        val literal = neo4jLiteral.toLiteral()
        val statement = createStatement(id = StatementId(1), `object` = literal)
        // Required to mock the Neo4Client DSL
        val mockUnboundRunnableSpec = mockk<UnboundRunnableSpec>()
        val mockMappingSpec = mockk<Neo4jClient.MappingSpec<ThingId>>()
        val mockRecordFetchSpec = mockk<Neo4jClient.RecordFetchSpec<ThingId>>()

        every { mockedNeo4jLiteralRepository.findById(literalId) }.returns(Optional.of(neo4jLiteral))
            .andThen(Optional.of(neo4jLiteral))
            .andThenAnswer { throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!") }
        every { neo4jClient.query(any<String>()) }.returns(mockUnboundRunnableSpec)
            .andThenAnswer { throw IllegalStateException("If you see this message, deleteByStatement() was called more than once!") }
        every { mockUnboundRunnableSpec.bindAll(any()) } returns mockUnboundRunnableSpec
        every { mockUnboundRunnableSpec.fetchAs(ThingId::class.java) } returns mockMappingSpec
        every { mockMappingSpec.mappedBy(any<BiFunction<TypeSystem, Record, ThingId>>()) } returns mockRecordFetchSpec
        every { mockRecordFetchSpec.one() } returns Optional.of(literalId)

        // Obtain literal from Literal repository
        assertThat(literalAdapter.findById(literalId).get()).isEqualTo(literal)
        // Verify the loading happened
        verify(exactly = 1) { mockedNeo4jLiteralRepository.findById(literalId) }

        // Obtain the same literal again for several times
        assertThat(literalAdapter.findById(literalId).get()).isEqualTo(literal)
        assertThat(literalAdapter.findById(literalId).get()).isEqualTo(literal)
        verify(exactly = 1) { mockedNeo4jLiteralRepository.findById(literalId) }

        // Delete statement along with literal
        statementAdapter.deleteByStatementId(statement.id)
        // Verify the deletion happened
        verify(exactly = 1) { neo4jClient.query(any<String>()) }
        verify(exactly = 1) { mockUnboundRunnableSpec.bindAll(any()) }
        verify(exactly = 1) { mockUnboundRunnableSpec.fetchAs(ThingId::class.java) }
        verify(exactly = 1) { mockMappingSpec.mappedBy(any<BiFunction<TypeSystem, Record, ThingId>>()) }
        verify(exactly = 1) { mockRecordFetchSpec.one() }

        // Verify that the cache was evicted
        assertThat(literalAdapter.findById(literalId).get()).isEqualTo(literal)
        verify(exactly = 2) { mockedNeo4jLiteralRepository.findById(literalId) }
    }

    @Configuration
    @EnableCaching(proxyTargetClass = true)
    class CachingTestConfig {
        @Bean
        fun cacheManager(): CacheManager = ConcurrentMapCacheManager(*allCacheNames)

        // This is the tricky part.
        // We are only interested in the interaction of the adapters and the cache, which means their logic must run.
        // That means we should use regular objects, but still need to "cut" the interaction with the database.
        // We do that by mocking the repositories.
        // For this to work, we need to ensure that the adapters use same instance of the mock.
        // The simplest way to do that is to use the DI capabilities of Spring, by just linking the beans.
        // We mock all components that we are not interested in.
        // They will fail if they are called accidentally, because of a missing stub, which is what we want.

        @Bean
        fun statementAdapter(
            cypherQueryBuilderFactory: CypherQueryBuilderFactory,
            neo4jClient: Neo4jClient,
            cacheManager: CacheManager,
        ): StatementRepository = SpringDataNeo4jStatementAdapter(
            // The stuff we care about:
            cypherQueryBuilderFactory = cypherQueryBuilderFactory,
            neo4jClient = neo4jClient,
            cacheManager = cacheManager,
            // We need to provide those, but do not care about them. (Except for not being called.)
            neo4jStatementIdGenerator = mockk(),
            predicateRepository = mockk()
        )

        @Bean
        fun literalAdapter(
            neo4jLiteralRepository: Neo4jLiteralRepository,
            neo4jClient: Neo4jClient,
        ): LiteralRepository =
            SpringDataNeo4jLiteralAdapter(
                neo4jRepository = neo4jLiteralRepository,
                neo4jLiteralIdGenerator = mockk(),
                cypherQueryBuilderFactory(neo4jClient)
            )

        // The "real" repositories used by both adapters, where we want to mock the interaction.
        @Bean
        fun neo4jLiteralRepository(): Neo4jLiteralRepository = mockk()

        @Bean
        fun neo4jClient(): Neo4jClient = mockk()

        @Bean
        fun cypherQueryBuilderFactory(neo4jClient: Neo4jClient): CypherQueryBuilderFactory =
            CypherQueryBuilderFactory(CypherConfiguration.defaultConfig(), neo4jClient)
    }
}
