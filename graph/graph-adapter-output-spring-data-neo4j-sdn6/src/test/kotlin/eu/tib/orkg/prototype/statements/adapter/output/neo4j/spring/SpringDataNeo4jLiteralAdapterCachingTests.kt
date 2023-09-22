package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import io.mockk.clearMocks
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
import org.orkg.statements.testing.createLiteral
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
    LITERAL_ID_TO_LITERAL_CACHE, LITERAL_ID_TO_LITERAL_EXISTS_CACHE,
    THING_ID_TO_THING_CACHE,
)

@ContextConfiguration
@ExtendWith(SpringExtension::class)
class SpringDataNeo4jLiteralAdapterCachingTests {

    private lateinit var mock: LiteralRepository

    @Autowired
    private lateinit var adapter: LiteralRepository

    @Autowired
    private lateinit var cacheManager: CacheManager

    @BeforeEach
    fun resetState() {
        allCacheNames.forEach { name ->
            // Reset the cache. Throw NPE if we cannot find the cache, most likely because the name is wrong.
            cacheManager.getCache(name)!!.clear()
        }

        // Obtain access to the proxied object, which is our mock created in the configuration below.
        mock = AopTestUtils.getTargetObject(adapter)
        // Clear the internal state of the mock, since Spring's Application context is not cleared between tests.
        clearMocks(mock)
    }

    @AfterEach
    fun verifyMocks() {
        // Verify that there we no more interactions with the repository
        confirmVerified(mock)
    }

    @Test
    fun `fetching a literal by ID should be cached`() {
        val literal = createLiteral().copy(id = ThingId("L1"))
        every { mock.findById(ThingId("L1")) } returns Optional.of(literal) andThenAnswer {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }

        // Obtain literal from repository
        assertThat(adapter.findById(ThingId("L1")).get()).isEqualTo(literal)
        // Verify the loading happened
        verify(exactly = 1) { mock.findById(ThingId("L1")) }

        // Obtain the same literal again for several times
        assertThat(adapter.findById(ThingId("L1")).get()).isEqualTo(literal)
        assertThat(adapter.findById(ThingId("L1")).get()).isEqualTo(literal)

        verify(exactly = 1) { mock.findById(ThingId("L1")) }
    }

    @Test
    fun `saving a literal should evict it from the id-to-literal cache`() {
        val literal = createLiteral().copy(id = ThingId("L1"))
        val modified = literal.copy(label = "new label")
        every { mock.findById(ThingId("L1")) } returns Optional.of(literal) andThen Optional.of(modified) andThenAnswer {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }
        every { mock.exists(ThingId("L1")) } returns true andThenAnswer {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }
        every { mock.save(modified) } returns Unit

        // Obtain literal from repository
        assertThat(adapter.findById(ThingId("L1")).get()).isEqualTo(literal)
        // Verify the loading happened
        verify(exactly = 1) { mock.findById(ThingId("L1")) }

        // Check literal existence in repository
        assertThat(adapter.exists(ThingId("L1"))).isTrue
        // Verify the loading happened
        verify(exactly = 1) { mock.exists(ThingId("L1")) }

        // Save a modified version
        adapter.save(modified)
        verify { mock.save(modified) } // required because of confirmVerified()

        // Obtaining the literal again
        assertThat(adapter.findById(ThingId("L1")).get())
            .`as`("obtaining the updated version from the cache")
            .isEqualTo(modified)
        // Verify the loading happened (again)
        verify(exactly = 2) { mock.findById(ThingId("L1")) }

        // Check literal existence again
        assertThat(adapter.exists(ThingId("L1"))).isTrue
        // Verify the loading did not happen again
        verify(exactly = 1) { mock.exists(ThingId("L1")) }
    }

    @Test
    fun `exists check of a literal by ID should be cached`() {
        every { mock.exists(ThingId("L1")) } returns true andThenAnswer {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }

        // Check literal existence in repository
        assertThat(adapter.exists(ThingId("L1"))).isTrue
        // Verify the loading happened
        verify(exactly = 1) { mock.exists(ThingId("L1")) }

        // Check existence of literal again for several times
        assertThat(adapter.exists(ThingId("L1"))).isTrue
        assertThat(adapter.exists(ThingId("L1"))).isTrue

        verify(exactly = 1) { mock.exists(ThingId("L1")) }
    }

    @Configuration
    @EnableCaching(proxyTargetClass = true)
    class CachingTestConfig {
        @Bean
        fun cacheManager(): CacheManager = ConcurrentMapCacheManager(*allCacheNames)

        @Bean
        fun mockedAdapter(): LiteralRepository = mockk<SpringDataNeo4jLiteralAdapter>()
    }
}
