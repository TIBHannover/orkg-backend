package org.orkg.graph.adapter.output.neo4j

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.testing.fixtures.createPredicate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.util.AopTestUtils

private val allCacheNames: Array<out String> = arrayOf(PREDICATE_ID_TO_PREDICATE_CACHE, THING_ID_TO_THING_CACHE)

@ContextConfiguration
@ExtendWith(SpringExtension::class)
internal class SpringDataNeo4jPredicateAdapterCachingTest : MockkBaseTest {

    private lateinit var mock: PredicateRepository

    @Autowired
    private lateinit var adapter: PredicateRepository

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
    }

    @Test
    fun `fetching a predicate by ID should be cached`() {
        val predicate = createPredicate(ThingId("P1"))
        every { mock.findById(ThingId("P1")) } returns Optional.of(predicate) andThenAnswer {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }

        // Obtain predicate from repository
        assertThat(adapter.findById(ThingId("P1")).get()).isEqualTo(predicate)
        // Verify the loading happened
        verify { mock.findById(ThingId("P1")) }

        // Obtain the same predicate again for several times
        assertThat(adapter.findById(ThingId("P1")).get()).isEqualTo(predicate)
        assertThat(adapter.findById(ThingId("P1")).get()).isEqualTo(predicate)
    }

    @Test
    fun `saving a predicate should evict it from the cache`() {
        val predicate = createPredicate(ThingId("P1"))
        val modified = predicate.copy(label = "new label")
        every { mock.findById(ThingId("P1")) } returns Optional.of(predicate) andThen Optional.of(modified) andThenAnswer {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }
        every { mock.save(modified) } returns Unit

        // Obtain predicate from repository
        assertThat(adapter.findById(ThingId("P1")).get()).isEqualTo(predicate)
        // Verify the loading happened
        verify(exactly = 1) { mock.findById(ThingId("P1")) }

        // Save a modified version
        adapter.save(modified)
        verify { mock.save(modified) } // required because of confirmVerified()

        // Obtaining the predicate again
        assertThat(adapter.findById(ThingId("P1")).get())
            .`as`("obtaining the updated version from the cache")
            .isEqualTo(modified)
        // Verify the loading happened (again)
        verify(exactly = 2) { mock.findById(ThingId("P1")) }
    }

    @Test
    fun `deleting a predicate should evict it from the cache`() {
        val predicate = createPredicate(ThingId("P1"))
        every { mock.findById(ThingId("P1")) } returns Optional.of(predicate) andThen Optional.of(predicate) andThenAnswer {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }
        every { adapter.deleteById(ThingId("P1")) } returns Unit

        // Obtain predicate from repository
        assertThat(adapter.findById(ThingId("P1")).get()).isEqualTo(predicate)
        // Verify the loading happened
        verify(exactly = 1) { mock.findById(ThingId("P1")) }

        // Obtain the same predicate again for several times
        assertThat(adapter.findById(ThingId("P1")).get()).isEqualTo(predicate)
        assertThat(adapter.findById(ThingId("P1")).get()).isEqualTo(predicate)
        verify(exactly = 1) { mock.findById(ThingId("P1")) }

        // Delete predicate from repository
        adapter.deleteById(ThingId("P1"))
        // Verify the deletion happened
        verify(exactly = 1) { mock.deleteById(ThingId("P1")) }

        // Verify that the cache was evicted
        assertThat(adapter.findById(ThingId("P1")).get()).isEqualTo(predicate)
        verify(exactly = 2) { mock.findById(ThingId("P1")) }
    }

    @Configuration
    @EnableCaching(proxyTargetClass = true)
    class CachingTestConfig {
        @Bean
        fun cacheManager(): CacheManager = ConcurrentMapCacheManager(*allCacheNames)

        @Bean
        fun mockedAdapter(): PredicateRepository = mockk<SpringDataNeo4jPredicateAdapter>()
    }
}
