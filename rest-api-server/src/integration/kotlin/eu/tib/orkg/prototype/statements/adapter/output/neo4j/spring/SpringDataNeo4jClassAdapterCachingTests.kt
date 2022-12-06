package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.createClass
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.spi.ClassRepository
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
class SpringDataNeo4jClassAdapterCachingTests {

    private lateinit var mock: ClassRepository

    @Autowired
    private lateinit var adapter: ClassRepository

    @Autowired
    private lateinit var cacheManager: CacheManager

    @BeforeEach
    fun resetState() {
        // Reset the cache. Throw NPE if we cannot find the cache, most likely because the name is wrong.
        cacheManager.getCache(CLASS_ID_TO_CLASS_CACHE)!!.clear()
        cacheManager.getCache(CLASS_ID_TO_CLASS_EXISTS_CACHE)!!.clear()

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
    fun `fetching a class by ID should be cached`() {
        val `class` = createClass().copy(id = ClassId("C1"))
        every { mock.findByClassId(ClassId("C1")) } returns Optional.of(`class`) andThen {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }

        // Obtain class from repository
        assertThat(adapter.findByClassId(ClassId("C1")).get()).isEqualTo(`class`)
        // Verify the loading happened
        verify(exactly = 1) { mock.findByClassId(ClassId("C1")) }

        // Obtain the same class again for several times
        assertThat(adapter.findByClassId(ClassId("C1")).get()).isEqualTo(`class`)
        assertThat(adapter.findByClassId(ClassId("C1")).get()).isEqualTo(`class`)

        verify(exactly = 1) { mock.findByClassId(ClassId("C1")) }
    }

    @Test
    fun `saving a class should evict it from the id-to-class cache`() {
        val `class` = createClass().copy(id = ClassId("C1"))
        val modified = `class`.copy(label = "new label")
        every { mock.findByClassId(ClassId("C1")) } returns Optional.of(`class`) andThen Optional.of(modified) andThen {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }
        every { mock.exists(ClassId("C1")) } returns true andThen {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }
        every { mock.save(modified) } returns Unit

        // Obtain class from repository
        assertThat(adapter.findByClassId(ClassId("C1")).get()).isEqualTo(`class`)
        // Verify the loading happened
        verify(exactly = 1) { mock.findByClassId(ClassId("C1")) }

        // Check class existence in repository
        assertThat(adapter.exists(ClassId("C1"))).isTrue
        // Verify the loading happened
        verify(exactly = 1) { mock.exists(ClassId("C1")) }

        // Save a modified version
        adapter.save(modified)
        verify { mock.save(modified) } // required because of confirmVerified()

        // Obtaining the class again
        assertThat(adapter.findByClassId(ClassId("C1")).get())
            .`as`("obtaining the updated version from the cache")
            .isEqualTo(modified)
        // Verify the loading happened (again)
        verify(exactly = 2) { mock.findByClassId(ClassId("C1")) }

        // Check class existence again
        assertThat(adapter.exists(ClassId("C1"))).isTrue
        // Verify the loading did not happen again
        verify(exactly = 1) { mock.exists(ClassId("C1")) }
    }

    @Test
    fun `exists check of a class by ID should be cached`() {
        every { mock.exists(ClassId("C1")) } returns true andThen {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }

        // Check class existence in repository
        assertThat(adapter.exists(ClassId("C1"))).isTrue
        // Verify the loading happened
        verify(exactly = 1) { mock.exists(ClassId("C1")) }

        // Check existence of class again for several times
        assertThat(adapter.exists(ClassId("C1"))).isTrue
        assertThat(adapter.exists(ClassId("C1"))).isTrue

        verify(exactly = 1) { mock.exists(ClassId("C1")) }
    }

    @Configuration
    @EnableCaching(proxyTargetClass = true)
    class CachingTestConfig {
        @Bean
        fun cacheManager(): CacheManager = ConcurrentMapCacheManager(CLASS_ID_TO_CLASS_CACHE, CLASS_ID_TO_CLASS_EXISTS_CACHE)

        @Bean
        fun mockedAdapter(): ClassRepository = mockk<SpringDataNeo4jClassAdapter>()
    }
}
