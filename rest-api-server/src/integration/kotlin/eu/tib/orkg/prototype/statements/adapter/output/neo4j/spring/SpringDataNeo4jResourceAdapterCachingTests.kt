package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
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
class SpringDataNeo4jResourceAdapterCachingTests {

    private lateinit var mock: ResourceRepository

    @Autowired
    private lateinit var adapter: ResourceRepository

    @Autowired
    private lateinit var cacheManager: CacheManager

    @BeforeEach
    fun resetState() {
        // Reset the cache. Throw NPE if we cannot find the cache, most likely because the name is wrong.
        cacheManager.getCache(RESOURCE_ID_TO_RESOURCE_CACHE)!!.clear()
        cacheManager.getCache(RESOURCE_ID_TO_RESOURCE_EXISTS_CACHE)!!.clear()

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
    fun `fetching a resource by ID should be cached`() {
        val resource = createResource().copy(id = ResourceId("R1"))
        every { mock.findByResourceId(ResourceId("R1")) } returns Optional.of(resource) andThen {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }

        // Obtain resource from repository
        assertThat(adapter.findByResourceId(ResourceId("R1")).get()).isEqualTo(resource)
        // Verify the loading happened
        verify(exactly = 1) { mock.findByResourceId(ResourceId("R1")) }

        // Obtain the same resource again for several times
        assertThat(adapter.findByResourceId(ResourceId("R1")).get()).isEqualTo(resource)
        assertThat(adapter.findByResourceId(ResourceId("R1")).get()).isEqualTo(resource)

        verify(exactly = 1) { mock.findByResourceId(ResourceId("R1")) }
    }

    @Test
    fun `saving a resource should evict it from the id-to-resource cache`() {
        val resource = createResource().copy(id = ResourceId("R1"))
        val modified = resource.copy(label = "new label")
        every { mock.findByResourceId(ResourceId("R1")) } returns Optional.of(resource) andThen Optional.of(modified) andThen {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }
        every { mock.exists(ResourceId("R1")) } returns true andThen {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }
        every { mock.save(modified) } returns modified

        // Obtain resource from repository
        assertThat(adapter.findByResourceId(ResourceId("R1")).get()).isEqualTo(resource)
        // Verify the loading happened
        verify(exactly = 1) { mock.findByResourceId(ResourceId("R1")) }

        // Check resource existence in repository
        assertThat(adapter.exists(ResourceId("R1"))).isTrue
        // Verify the loading happened
        verify(exactly = 1) { mock.exists(ResourceId("R1")) }

        // Save a modified version
        adapter.save(modified)
        verify { mock.save(modified) } // required because of confirmVerified()

        // Obtaining the resource again
        assertThat(adapter.findByResourceId(ResourceId("R1")).get())
            .`as`("obtaining the updated version from the cache")
            .isEqualTo(modified)
        // Verify the loading happened (again)
        verify(exactly = 2) { mock.findByResourceId(ResourceId("R1")) }

        // Check resource existence again
        assertThat(adapter.exists(ResourceId("R1"))).isTrue
        // Verify the loading did not happen again
        verify(exactly = 1) { mock.exists(ResourceId("R1")) }
    }

    @Test
    fun `exists check of a resource by ID should be cached`() {
        every { mock.exists(ResourceId("R1")) } returns true andThen {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }

        // Check resource existence in repository
        assertThat(adapter.exists(ResourceId("R1"))).isTrue
        // Verify the loading happened
        verify(exactly = 1) { mock.exists(ResourceId("R1")) }

        // Check existence of resource again for several times
        assertThat(adapter.exists(ResourceId("R1"))).isTrue
        assertThat(adapter.exists(ResourceId("R1"))).isTrue

        verify(exactly = 1) { mock.exists(ResourceId("R1")) }
    }

    @Test
    fun `deleting a resource should evict it from the id-to-resource cache`() {
        val resource = createResource().copy(id = ResourceId("R1"))
        every { mock.findByResourceId(ResourceId("R1")) } returns Optional.of(resource) andThen Optional.of(resource) andThen {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }
        every { adapter.deleteByResourceId(ResourceId("R1")) } returns Unit

        // Obtain predicate from repository
        assertThat(adapter.findByResourceId(ResourceId("R1")).get()).isEqualTo(resource)
        // Verify the loading happened
        verify(exactly = 1) { mock.findByResourceId(ResourceId("R1")) }

        // Obtain the same predicate again for several times
        assertThat(adapter.findByResourceId(ResourceId("R1")).get()).isEqualTo(resource)
        assertThat(adapter.findByResourceId(ResourceId("R1")).get()).isEqualTo(resource)
        verify(exactly = 1) { mock.findByResourceId(ResourceId("R1")) }

        // Delete predicate from repository
        adapter.deleteByResourceId(ResourceId("R1"))
        // Verify the deletion happened
        verify(exactly = 1) { mock.deleteByResourceId(ResourceId("R1")) }

        // Verify that the cache was evicted
        assertThat(adapter.findByResourceId(ResourceId("R1")).get()).isEqualTo(resource)
        verify(exactly = 2) { mock.findByResourceId(ResourceId("R1")) }
    }

    @Test
    fun `deleting a resource should evict it from the id-to-resource-exists cache`() {
        every { mock.exists(ResourceId("R1")) } returns true andThen false andThen {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }
        every { adapter.deleteByResourceId(ResourceId("R1")) } returns Unit

        // Obtain predicate from repository
        assertThat(adapter.exists(ResourceId("R1"))).isTrue
        // Verify the loading happened
        verify(exactly = 1) { mock.exists(ResourceId("R1")) }

        // Obtain the same predicate again for several times
        assertThat(adapter.exists(ResourceId("R1"))).isTrue
        assertThat(adapter.exists(ResourceId("R1"))).isTrue
        verify(exactly = 1) { mock.exists(ResourceId("R1")) }

        // Delete predicate from repository
        adapter.deleteByResourceId(ResourceId("R1"))
        // Verify the deletion happened
        verify(exactly = 1) { mock.deleteByResourceId(ResourceId("R1")) }

        // Verify that the cache was evicted
        assertThat(adapter.exists(ResourceId("R1"))).isFalse
        verify(exactly = 2) { mock.exists(ResourceId("R1")) }
    }

    @Configuration
    @EnableCaching(proxyTargetClass = true)
    class CachingTestConfig {
        @Bean
        fun cacheManager(): CacheManager = ConcurrentMapCacheManager(RESOURCE_ID_TO_RESOURCE_CACHE, RESOURCE_ID_TO_RESOURCE_EXISTS_CACHE)

        @Bean
        fun mockedAdapter(): ResourceRepository = mockk<SpringDataNeo4jResourceAdapter>()
    }
}
