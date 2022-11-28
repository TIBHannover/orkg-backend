package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.createPredicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.util.AopTestUtils

@ContextConfiguration
@ExtendWith(SpringExtension::class)
class SpringDataNeo4jPredicateAdapterCachingTests {

    private lateinit var mock: PredicateRepository

    @Autowired
    private lateinit var adapter: PredicateRepository

    @Autowired
    private lateinit var cacheManager: CacheManager

    @BeforeEach
    fun resetState() {
        // Reset the cache. Throw NPE if we cannot find the cache, most likely because the name is wrong.
        cacheManager.getCache("predicates")!!.clear()

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
    fun `fetching a predicate by ID should be cached`() {
        val predicate = createPredicate().copy(id = PredicateId("P1"))
        every { mock.findByPredicateId(PredicateId("P1")) } returns Optional.of(predicate) andThen {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }

        // Obtain predicate from repository
        assertThat(adapter.findByPredicateId(PredicateId("P1")).get()).isEqualTo(predicate)
        // Verify the loading happened
        verify { mock.findByPredicateId(PredicateId("P1")) }

        // Obtain the same predicate again for several times
        assertThat(adapter.findByPredicateId(PredicateId("P1")).get()).isEqualTo(predicate)
        assertThat(adapter.findByPredicateId(PredicateId("P1")).get()).isEqualTo(predicate)
    }

    @Test
    fun `saving a predicate should evict it from the cache`() {
        val predicate = createPredicate().copy(id = PredicateId("P1"))
        val modified = predicate.copy(label = "new label")
        every { mock.findByPredicateId(PredicateId("P1")) } returns Optional.of(predicate) andThen Optional.of(modified) andThen {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }
        every { mock.save(modified) } returns Unit

        // Obtain predicate from repository
        assertThat(adapter.findByPredicateId(PredicateId("P1")).get()).isEqualTo(predicate)
        // Verify the loading happened
        verify(exactly = 1) { mock.findByPredicateId(PredicateId("P1")) }

        // Save a modified version
        adapter.save(modified)
        verify { mock.save(modified) } // required because of confirmVerified()

        // Obtaining the predicate again
        assertThat(adapter.findByPredicateId(PredicateId("P1")).get())
            .`as`("obtaining the updated version from the cache")
            .isEqualTo(modified)
        // Verify the loading happened (again)
        verify(exactly = 2) { mock.findByPredicateId(PredicateId("P1")) }
    }

    @Configuration
    @EnableCaching(proxyTargetClass = true)
    class CachingTestConfig {
        @Bean
        fun cacheManager(): CacheManager = ConcurrentMapCacheManager("predicates")

        @Bean
        fun mockedAdapter(): PredicateRepository = mockk<SpringDataNeo4jPredicateAdapter>()
    }
}
