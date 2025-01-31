package org.orkg.statistics.domain

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.concurrent.Callable
import org.junit.jupiter.api.Test
import org.orkg.common.exceptions.UnknownParameter
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager

internal class CachedMetricTest : MockkBaseTest {

    @Test
    fun `Given a cached metric, retrieving its value without parameters, it returns the value`() {
        val mockMetricSupplier: (ParameterMap) -> Number = mockk()
        val cacheManager: CacheManager = mockk()
        val cache: Cache = mockk()

        every { cacheManager.getCache(METRICS_NAME_AND_PARAMETERS_TO_VALUE_CACHE) } returns cache

        val metric = CachedMetric.create(
            cacheManager = cacheManager,
            name = "test",
            group = "test",
            description = "test description",
            supplier = mockMetricSupplier
        )
        val parameters = emptyMap<String, String>()

        verify(exactly = 1) { cacheManager.getCache(METRICS_NAME_AND_PARAMETERS_TO_VALUE_CACHE) }

        every { cache.get("test-test:[]", any<Callable<Number>>()) } returns 5

        metric.value(parameters) shouldBe 5

        verify(exactly = 1) { cache.get("test-test:[]", any<Callable<Number>>()) }
    }

    @Test
    fun `Given a cached metric, retrieving its value and cache returns null, it returns the value`() {
        val mockMetricSupplier: (ParameterMap) -> Number = mockk()
        val cacheManager: CacheManager = mockk()
        val cache: Cache = mockk()

        every { cacheManager.getCache(METRICS_NAME_AND_PARAMETERS_TO_VALUE_CACHE) } returns cache

        val metric = CachedMetric.create(
            cacheManager = cacheManager,
            name = "test",
            group = "test",
            description = "test description",
            supplier = mockMetricSupplier
        )
        val parameters = emptyMap<String, String>()

        verify(exactly = 1) { cacheManager.getCache(METRICS_NAME_AND_PARAMETERS_TO_VALUE_CACHE) }

        every { cache.get("test-test:[]", any<Callable<Number>>()) } returns null
        every { mockMetricSupplier(any()) } returns 5

        metric.value(parameters) shouldBe 5

        verify(exactly = 1) { cache.get("test-test:[]", any<Callable<Number>>()) }
        verify(exactly = 1) { mockMetricSupplier(any()) }
    }

    @Test
    fun `Given a cached metric, retrieving its value with parameters, it returns the value`() {
        val mockMetricSupplier: (ParameterMap) -> Number = mockk()
        val cacheManager: CacheManager = mockk()
        val cache: Cache = mockk()

        every { cacheManager.getCache(METRICS_NAME_AND_PARAMETERS_TO_VALUE_CACHE) } returns cache

        val intParameterSpec = ParameterSpec(
            name = "test",
            description = "test description",
            type = Int::class,
            parser = { it.toInt() }
        )
        val metric = CachedMetric.create(
            cacheManager = cacheManager,
            name = "test",
            group = "test",
            description = "test description",
            parameterSpecs = mapOf("id" to intParameterSpec),
            supplier = mockMetricSupplier
        )
        val parameters = mapOf("id" to "123")

        verify(exactly = 1) { cacheManager.getCache(METRICS_NAME_AND_PARAMETERS_TO_VALUE_CACHE) }

        every { cache.get("test-test:[id=123]", any<Callable<Number>>()) } returns 5

        metric.value(parameters) shouldBe 5

        verify(exactly = 1) { cache.get("test-test:[id=123]", any<Callable<Number>>()) }
    }

    @Test
    fun `Given a cached metric, retrieving its value with an unknown parameter, it throws an exception`() {
        val mockMetricSupplier: (ParameterMap) -> Number = mockk()
        val cacheManager: CacheManager = mockk()
        val cache: Cache = mockk()

        every { cacheManager.getCache(METRICS_NAME_AND_PARAMETERS_TO_VALUE_CACHE) } returns cache

        val metric = CachedMetric.create(
            cacheManager = cacheManager,
            name = "test",
            group = "test",
            description = "test description",
            supplier = mockMetricSupplier
        )
        val parameters = mapOf("filter" to "value")

        verify(exactly = 1) { cacheManager.getCache(METRICS_NAME_AND_PARAMETERS_TO_VALUE_CACHE) }

        every { cache.get("test-test:[filter=value]", any<Callable<Number>>()) } throws UnknownParameter("filter")

        shouldThrow<UnknownParameter> { metric.value(parameters) }.asClue {
            it.message shouldBe """Unknown parameter "filter"."""
        }

        verify(exactly = 1) { cache.get("test-test:[filter=value]", any<Callable<Number>>()) }
    }
}
