package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.ThingRepository
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
import org.orkg.statements.testing.createClass
import org.orkg.statements.testing.createLiteral
import org.orkg.statements.testing.createPredicate
import org.orkg.statements.testing.createResource
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
class SpringDataNeo4jThingAdapterCachingTests {

    private lateinit var mock: ThingRepository

    @Autowired
    private lateinit var adapter: ThingRepository

    @Autowired
    private lateinit var cacheManager: CacheManager

    // Supportive doubles and adapters, because the caches interact

    private lateinit var classMock: ClassRepository
    private lateinit var literalMock: LiteralRepository
    private lateinit var predicateMock: PredicateRepository
    private lateinit var resourceMock: ResourceRepository

    @Autowired
    private lateinit var classAdapter: ClassRepository

    @Autowired
    private lateinit var literalAdapter: LiteralRepository

    @Autowired
    private lateinit var predicateAdapter: PredicateRepository

    @Autowired
    private lateinit var resourceAdapter: ResourceRepository

    @BeforeEach
    fun resetState() {
        allCacheNames.forEach { name ->
            // Reset the cache. Throw NPE if we cannot find the cache, most likely because the name is wrong.
            cacheManager.getCache(name)!!.clear()
        }

        // Obtain access to the proxied object, which is our mock created in the configuration below.
        mock = AopTestUtils.getTargetObject(adapter)
        classMock = AopTestUtils.getTargetObject(classAdapter)
        literalMock = AopTestUtils.getTargetObject(literalAdapter)
        predicateMock = AopTestUtils.getTargetObject(predicateAdapter)
        resourceMock = AopTestUtils.getTargetObject(resourceAdapter)

        // Clear the internal state of the mock, since Spring's Application context is not cleared between tests.
        clearMocks(mock, classMock, literalMock, predicateMock, resourceMock)
    }

    @AfterEach
    fun verifyMocks() {
        // Verify that there we no more interactions with the repository
        confirmVerified(mock)
    }

    @Test
    fun `fetching a thing by ID should be cached`() {
        val thingId = "R1"
        val resource = createResource().copy(id = ResourceId(thingId))
        every { mock.findByThingId("R1") } returns Optional.of(resource) andThen {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }

        // Obtain resource from repository
        assertThat(adapter.findByThingId(thingId).get()).isEqualTo(resource)
        // Verify the loading happened
        verify(exactly = 1) { mock.findByThingId(thingId) }

        // Obtain the same resource again for several times
        assertThat(adapter.findByThingId(thingId).get()).isEqualTo(resource)
        assertThat(adapter.findByThingId(thingId).get()).isEqualTo(resource)

        verify(exactly = 1) { mock.findByThingId(thingId) }
    }

    @Test
    fun `saving a class should evict it from the thing-id cache`() {
        val thingId = "R1"
        val `class` = createClass().copy(id = ClassId(thingId))
        val modified = `class`.copy(label = "new label")
        every { mock.findByThingId(thingId) } returns Optional.of(`class`) andThen Optional.of(modified) andThen {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }
        every { classMock.save(modified) } returns Unit

        // Obtain class from Thing repository
        assertThat(adapter.findByThingId("R1").get()).isEqualTo(`class`)
        // Verify the loading happened
        verify(exactly = 1) { mock.findByThingId(thingId) }

        // Save a modified version
        classAdapter.save(modified)
        verify { classAdapter.save(modified) } // required because of confirmVerified()

        // Obtaining the class again
        assertThat(adapter.findByThingId(thingId).get()).`as`("obtaining the updated version from the repository")
            .isEqualTo(modified)
        // Verify the loading happened (again)
        verify(exactly = 2) { mock.findByThingId(thingId) }
    }

    @Test
    fun `saving a literal should evict it from the thing-id cache`() {
        val thingId = "R1"
        val literal = createLiteral().copy(id = LiteralId(thingId))
        val modified = literal.copy(label = "new label")
        every { mock.findByThingId(thingId) } returns Optional.of(literal) andThen Optional.of(modified) andThen {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }
        every { literalMock.save(modified) } returns Unit

        // Obtain literal from Thing repository
        assertThat(adapter.findByThingId("R1").get()).isEqualTo(literal)
        // Verify the loading happened
        verify(exactly = 1) { mock.findByThingId(thingId) }

        // Save a modified version
        literalAdapter.save(modified)
        verify { literalAdapter.save(modified) } // required because of confirmVerified()

        // Obtaining the literal again
        assertThat(adapter.findByThingId(thingId).get()).`as`("obtaining the updated version from the repository")
            .isEqualTo(modified)
        // Verify the loading happened (again)
        verify(exactly = 2) { mock.findByThingId(thingId) }
    }

    @Test
    fun `saving a predicate should evict it from the thing-id cache`() {
        val thingId = "R1"
        val predicate = createPredicate().copy(id = PredicateId(thingId))
        val modified = predicate.copy(label = "new label")
        every { mock.findByThingId(thingId) } returns Optional.of(predicate) andThen Optional.of(modified) andThen {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }
        every { predicateMock.save(modified) } returns Unit

        // Obtain predicate from Thing repository
        assertThat(adapter.findByThingId("R1").get()).isEqualTo(predicate)
        // Verify the loading happened
        verify(exactly = 1) { mock.findByThingId(thingId) }

        // Save a modified version
        predicateAdapter.save(modified)
        verify { predicateAdapter.save(modified) } // required because of confirmVerified()

        // Obtaining the predicate again
        assertThat(adapter.findByThingId(thingId).get()).`as`("obtaining the updated version from the repository")
            .isEqualTo(modified)
        // Verify the loading happened (again)
        verify(exactly = 2) { mock.findByThingId(thingId) }
    }

    @Test
    fun `saving a resource should evict it from the thing-id cache`() {
        val thingId = "R1"
        val resource = createResource().copy(id = ResourceId(thingId))
        val modified = resource.copy(label = "new label")
        every { mock.findByThingId(thingId) } returns Optional.of(resource) andThen Optional.of(modified) andThen {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }
        every { resourceMock.save(modified) } returns Unit

        // Obtain resource from Thing repository
        assertThat(adapter.findByThingId("R1").get()).isEqualTo(resource)
        // Verify the loading happened
        verify(exactly = 1) { mock.findByThingId(thingId) }

        // Save a modified version
        resourceAdapter.save(modified)
        verify { resourceAdapter.save(modified) } // required because of confirmVerified()

        // Obtaining the resource again
        assertThat(adapter.findByThingId(thingId).get())
            .`as`("obtaining the updated version from the repository")
            .isEqualTo(modified)
        // Verify the loading happened (again)
        verify(exactly = 2) { mock.findByThingId(thingId) }
    }

    @Test
    fun `deleting a predicate should evict it from the thing cache`() {
        val thingId = "R1"
        val predicate = createPredicate().copy(id = PredicateId(thingId))
        every { mock.findByThingId(thingId) } returns Optional.of(predicate) andThen Optional.of(predicate) andThen {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }
        every { predicateMock.deleteByPredicateId(predicate.id!!) } returns Unit

        // Obtain predicate from Thing repository
        assertThat(adapter.findByThingId(thingId).get()).isEqualTo(predicate)
        // Verify the loading happened
        verify(exactly = 1) { mock.findByThingId(thingId) }

        // Obtain the same predicate again for several times
        assertThat(adapter.findByThingId(thingId).get()).isEqualTo(predicate)
        assertThat(adapter.findByThingId(thingId).get()).isEqualTo(predicate)
        verify(exactly = 1) { mock.findByThingId(thingId) }

        // Delete predicate from repository
        predicateAdapter.deleteByPredicateId(predicate.id!!)
        // Verify the deletion happened
        verify(exactly = 1) { predicateMock.deleteByPredicateId(predicate.id!!) }

        // Verify that the cache was evicted
        assertThat(adapter.findByThingId(thingId).get()).isEqualTo(predicate)
        verify(exactly = 2) { mock.findByThingId(thingId) }
    }

    @Test
    fun `deleting a resource should evict it from the thing cache`() {
        val thingId = "R1"
        val resource = createResource().copy(id = ResourceId(thingId))
        every { mock.findByThingId(thingId) } returns Optional.of(resource) andThen Optional.of(resource) andThen {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }
        every { resourceMock.deleteByResourceId(resource.id!!) } returns Unit

        // Obtain resource from Thing repository
        assertThat(adapter.findByThingId(thingId).get()).isEqualTo(resource)
        // Verify the loading happened
        verify(exactly = 1) { mock.findByThingId(thingId) }

        // Obtain the same predicate again for several times
        assertThat(adapter.findByThingId(thingId).get()).isEqualTo(resource)
        assertThat(adapter.findByThingId(thingId).get()).isEqualTo(resource)
        verify(exactly = 1) { mock.findByThingId(thingId) }

        // Delete predicate from repository
        resourceAdapter.deleteByResourceId(resource.id!!)
        // Verify the deletion happened
        verify(exactly = 1) { resourceMock.deleteByResourceId(resource.id!!) }

        // Verify that the cache was evicted
        assertThat(adapter.findByThingId(thingId).get()).isEqualTo(resource)
        verify(exactly = 2) { mock.findByThingId(thingId) }
    }

    @Configuration
    @EnableCaching(proxyTargetClass = true)
    class CachingTestConfig {
        @Bean
        fun cacheManager(): CacheManager = ConcurrentMapCacheManager(*allCacheNames)

        @Bean
        fun mockedAdapter(): ThingRepository = mockk<SpringDataNeo4jThingAdapter>()

        // Supportive adapters / mocks

        @Bean
        fun mockedClassAdapter(): ClassRepository = mockk<SpringDataNeo4jClassAdapter>()

        @Bean
        fun mockedLiteralAdapter(): LiteralRepository = mockk<SpringDataNeo4jLiteralAdapter>()

        @Bean
        fun mockedPredicateAdapter(): PredicateRepository = mockk<SpringDataNeo4jPredicateAdapter>()

        @Bean
        fun mockedResourceAdapter(): ResourceRepository = mockk<SpringDataNeo4jResourceAdapter>()
    }
}
