package org.orkg.graph.adapter.output.neo4j

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.testing.fixtures.createClass
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.util.AopTestUtils
import java.util.Optional

private val allCacheNames: Array<out String> = arrayOf(
    CLASS_ID_TO_CLASS_CACHE,
    CLASS_ID_TO_CLASS_EXISTS_CACHE,
    THING_ID_TO_THING_CACHE,
)

@ContextConfiguration
@ExtendWith(SpringExtension::class)
internal class SpringDataNeo4jClassAdapterCachingTest : MockkBaseTest {
    private lateinit var mock: ClassRepository

    @Autowired
    private lateinit var adapter: ClassRepository

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
    fun `fetching a class by ID should be cached`() {
        val `class` = createClass(ThingId("C1"))
        every { mock.findById(ThingId("C1")) } returns Optional.of(`class`) andThenAnswer {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }

        // Obtain class from repository
        assertThat(adapter.findById(ThingId("C1")).get()).isEqualTo(`class`)
        // Verify the loading happened
        verify(exactly = 1) { mock.findById(ThingId("C1")) }

        // Obtain the same class again for several times
        assertThat(adapter.findById(ThingId("C1")).get()).isEqualTo(`class`)
        assertThat(adapter.findById(ThingId("C1")).get()).isEqualTo(`class`)

        verify(exactly = 1) { mock.findById(ThingId("C1")) }
    }

    @Test
    fun `saving a class should evict it from the id-to-class cache`() {
        val `class` = createClass(ThingId("C1"))
        val modified = `class`.copy(label = "new label")
        every { mock.findById(ThingId("C1")) } returns Optional.of(`class`) andThen Optional.of(modified) andThenAnswer {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }
        every { mock.existsById(ThingId("C1")) } returns true andThenAnswer {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }
        every { mock.save(modified) } returns Unit

        // Obtain class from repository
        assertThat(adapter.findById(ThingId("C1")).get()).isEqualTo(`class`)
        // Verify the loading happened
        verify(exactly = 1) { mock.findById(ThingId("C1")) }

        // Check class existence in repository
        assertThat(adapter.existsById(ThingId("C1"))).isTrue
        // Verify the loading happened
        verify(exactly = 1) { mock.existsById(ThingId("C1")) }

        // Save a modified version
        adapter.save(modified)
        verify { mock.save(modified) } // required because of confirmVerified()

        // Obtaining the class again
        assertThat(adapter.findById(ThingId("C1")).get())
            .`as`("obtaining the updated version from the cache")
            .isEqualTo(modified)
        // Verify the loading happened (again)
        verify(exactly = 2) { mock.findById(ThingId("C1")) }

        // Check class existence again
        assertThat(adapter.existsById(ThingId("C1"))).isTrue
        // Verify the loading did not happen again
        verify(exactly = 1) { mock.existsById(ThingId("C1")) }
    }

    @Test
    fun `exists check of a class by ID should be cached`() {
        every { mock.existsById(ThingId("C1")) } returns true andThenAnswer {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }

        // Check class existence in repository
        assertThat(adapter.existsById(ThingId("C1"))).isTrue
        // Verify the loading happened
        verify(exactly = 1) { mock.existsById(ThingId("C1")) }

        // Check existence of class again for several times
        assertThat(adapter.existsById(ThingId("C1"))).isTrue
        assertThat(adapter.existsById(ThingId("C1"))).isTrue

        verify(exactly = 1) { mock.existsById(ThingId("C1")) }
    }

    @Configuration
    @EnableCaching(proxyTargetClass = true)
    class CachingTestConfig {
        @Bean
        fun cacheManager(): CacheManager = ConcurrentMapCacheManager(*allCacheNames)

        @Bean
        fun mockedAdapter(): ClassRepository = mockk<SpringDataNeo4jClassAdapter>()
    }
}
