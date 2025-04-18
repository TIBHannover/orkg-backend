package org.orkg.contenttypes.adapter.output.simcomp

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.testing.fixtures.createPublishedContentType
import org.orkg.contenttypes.output.LiteratureListPublishedRepository
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
    THING_ID_TO_PUBLISHED_LITERATURE_LIST_CACHE
)

@ContextConfiguration
@ExtendWith(SpringExtension::class)
internal class SimCompLiteratureListPublishedAdapterCachingTest : MockkBaseTest {
    private lateinit var mock: LiteratureListPublishedRepository

    @Autowired
    private lateinit var adapter: LiteratureListPublishedRepository

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
    fun `fetching a published content type by ID should be cached`() {
        val contentType = createPublishedContentType()
        every { mock.findById(ThingId("R1")) } returns Optional.of(contentType) andThenAnswer {
            throw IllegalStateException("If you see this message, the method was called more often than expected: Caching did not work!")
        }

        // Obtain resource from repository
        assertThat(adapter.findById(ThingId("R1")).get()).isEqualTo(contentType)
        // Verify the loading happened
        verify(exactly = 1) { mock.findById(ThingId("R1")) }

        // Obtain the same resource again for several times
        assertThat(adapter.findById(ThingId("R1")).get()).isEqualTo(contentType)
        assertThat(adapter.findById(ThingId("R1")).get()).isEqualTo(contentType)

        verify(exactly = 1) { mock.findById(ThingId("R1")) }
    }

    @Configuration
    @EnableCaching(proxyTargetClass = true)
    class CachingTestConfig {
        @Bean
        fun cacheManager(): CacheManager = ConcurrentMapCacheManager(*allCacheNames)

        @Bean
        fun mockedAdapter(): LiteratureListPublishedRepository =
            mockk<SimCompLiteratureListPublishedAdapter>()
    }
}
