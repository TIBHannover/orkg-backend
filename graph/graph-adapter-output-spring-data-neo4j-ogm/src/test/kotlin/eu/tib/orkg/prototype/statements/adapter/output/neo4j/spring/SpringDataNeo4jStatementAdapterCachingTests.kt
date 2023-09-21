package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jLiteralRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jStatementRepository
import eu.tib.orkg.prototype.statements.createNeo4jLiteral
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.orkg.statements.testing.createStatement
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.util.AopTestUtils

private val allCacheNames: Array<out String> = arrayOf(
    THING_ID_TO_THING_CACHE,
    CLASS_ID_TO_CLASS_CACHE, CLASS_ID_TO_CLASS_EXISTS_CACHE,
    LITERAL_ID_TO_LITERAL_CACHE, LITERAL_ID_TO_LITERAL_EXISTS_CACHE,
    PREDICATE_ID_TO_PREDICATE_CACHE,
    RESOURCE_ID_TO_RESOURCE_CACHE, RESOURCE_ID_TO_RESOURCE_EXISTS_CACHE,
)

@ContextConfiguration
@ExtendWith(SpringExtension::class)
internal class SpringDataNeo4jStatementAdapterCachingTests {

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
    private lateinit var neo4jStatementRepository: Neo4jStatementRepository

    @Autowired
    private lateinit var neo4jLiteralRepository: Neo4jLiteralRepository

    // These references will hold references to the mocks that are wrapped with the caching logic:

    private lateinit var mockedNeo4jStatementRepository: Neo4jStatementRepository

    private lateinit var mockedNeo4jLiteralRepository: Neo4jLiteralRepository

    @BeforeEach
    fun resetState() {
        allCacheNames.forEach { name ->
            // Reset the cache. Throw NPE if we cannot find the cache, most likely because the name is wrong.
            cacheManager.getCache(name)!!.clear()
        }

        // Obtain access to the proxied object, which is our mock created in the configuration below.
        mockedNeo4jStatementRepository = AopTestUtils.getTargetObject(neo4jStatementRepository)
        mockedNeo4jLiteralRepository = AopTestUtils.getTargetObject(neo4jLiteralRepository)

        // Clear the internal state of the mock, since Spring's Application context is not cleared between tests.
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        // Verify that there we no more interactions with the repository
        confirmVerified(mockedNeo4jStatementRepository, mockedNeo4jLiteralRepository)
    }

    @Test
    fun `deleting a statement with a literal object should evict the literal from the literal cache`() {
        val literalId = ThingId("L1")
        val neo4jLiteral = createNeo4jLiteral(id = literalId)
        val literal = neo4jLiteral.toLiteral()
        val statement = createStatement(
            id = StatementId(1), `object` = literal
        )
        every { mockedNeo4jLiteralRepository.findById(literalId) }.returns(Optional.of(neo4jLiteral))
            .andThen(Optional.of(neo4jLiteral))
            .andThen { throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!") }
        every { mockedNeo4jStatementRepository.deleteByStatementId(statement.id!!) }.returns(Optional.of(literalId))
            .andThen { throw IllegalStateException("If you see this message, deleteByStatement() was called more than once!") }

        // Obtain literal from Literal repository
        assertThat(literalAdapter.findById(literalId).get()).isEqualTo(literal)
        // Verify the loading happened
        verify(exactly = 1) { mockedNeo4jLiteralRepository.findById(literalId) }

        // Obtain the same literal again for several times
        assertThat(literalAdapter.findById(literalId).get()).isEqualTo(literal)
        assertThat(literalAdapter.findById(literalId).get()).isEqualTo(literal)
        verify(exactly = 1) { mockedNeo4jLiteralRepository.findById(literalId) }

        // Delete statement along with literal
        statementAdapter.deleteByStatementId(statement.id!!)
        // Verify the deletion happened
        verify(exactly = 1) { mockedNeo4jStatementRepository.deleteByStatementId(statement.id!!) }

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
            neo4jStatementRepository: Neo4jStatementRepository,
            neo4jLiteralRepository: Neo4jLiteralRepository,
            cacheManager: CacheManager
        ): StatementRepository = SpringDataNeo4jStatementAdapter(
            // The stuff we care about:
            neo4jRepository = neo4jStatementRepository,
            neo4jLiteralRepository = neo4jLiteralRepository,
            cacheManager = cacheManager,
            // We need to provide those, but do not care about them. (Except for not being called.)
            neo4jStatementIdGenerator = mockk(),
            neo4jResourceRepository = mockk(),
            neo4jPredicateRepository = mockk(),
            neo4jClassRepository = mockk(),
        )

        @Bean
        fun literalAdapter(neo4jLiteralRepository: Neo4jLiteralRepository): LiteralRepository =
            SpringDataNeo4jLiteralAdapter(
                neo4jRepository = neo4jLiteralRepository,
                neo4jLiteralIdGenerator = mockk(),
            )

        // The "real" repositories used by both adapters, where we want to mock the interaction.
        @Bean
        fun neo4jLiteralRepository(): Neo4jLiteralRepository = mockk()

        @Bean
        fun neo4jStatementRepository(): Neo4jStatementRepository = mockk()
    }
}