package org.orkg.statistics.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.exceptions.UnknownParameter
import org.orkg.common.testing.fixtures.MockkBaseTest

internal class SimpleMetricTest : MockkBaseTest {
    @Test
    fun `Given a simple metric, retrieving its value without parameters, it returns the value`() {
        val mockMetricSupplier: (ParameterMap) -> Number = mockk()
        val metric = SimpleMetric(
            name = "test",
            group = "test",
            description = "test description",
            supplier = mockMetricSupplier
        )
        val parameters = emptyMap<String, String>()

        every { mockMetricSupplier(any()) } returns 5

        metric.value(parameters) shouldBe 5

        verify(exactly = 1) { mockMetricSupplier(any()) }
    }

    @Test
    fun `Given a simple metric, retrieving its value with parameters, it returns the value`() {
        val mockMetricSupplier: (ParameterMap) -> Number = mockk()
        val intParameterSpec = ParameterSpec(
            name = "test",
            description = "test description",
            type = Int::class,
            parser = { it.toInt() }
        )
        val metric = SimpleMetric(
            name = "test",
            group = "test",
            description = "test description",
            parameterSpecs = mapOf("id" to intParameterSpec),
            supplier = mockMetricSupplier
        )
        val parameters = mapOf("id" to "123")

        every { mockMetricSupplier(any()) } returns 5

        metric.value(parameters) shouldBe 5

        verify(exactly = 1) { mockMetricSupplier(withArg { it[intParameterSpec] shouldBe 123 }) }
    }

    @Test
    fun `Given a simple metric, retrieving its value with an unknown parameter, it throws an exception`() {
        val mockMetricSupplier: (ParameterMap) -> Number = mockk()
        val metric = SimpleMetric(
            name = "test",
            group = "test",
            description = "test description",
            supplier = mockMetricSupplier
        )
        val parameters = mapOf("id" to "R123")

        shouldThrow<UnknownParameter> { metric.value(parameters) }
    }
}
