package org.orkg.contenttypes.domain.actions.comparisons

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ComparisonAlreadyPublished
import org.orkg.contenttypes.domain.ComparisonInUse
import org.orkg.contenttypes.domain.ComparisonNotModifiable
import org.orkg.contenttypes.domain.actions.DeleteComparisonState
import org.orkg.contenttypes.input.testing.fixtures.deleteComparisonCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

internal class ComparisonDeleteValidatorUnitTest : MockkBaseTest {
    private val thingRepository: ThingRepository = mockk()

    private val comparisonDeleteValidator = ComparisonDeleteValidator(thingRepository)

    @Test
    fun `Given a comparison delete command, when validation succeeds, it returns success`() {
        val comparisonId = ThingId("R8186")
        val command = deleteComparisonCommand().copy(comparisonId = comparisonId)
        val state = DeleteComparisonState(comparison = createResource(id = comparisonId))

        every { thingRepository.isUsedAsObject(comparisonId) } returns false

        comparisonDeleteValidator(command, state) shouldBe state

        verify(exactly = 1) { thingRepository.isUsedAsObject(comparisonId) }
    }

    @Test
    fun `Given a comparison delete command, when comparison is not modifiable, it throws an exception`() {
        val comparisonId = ThingId("R8186")
        val command = deleteComparisonCommand().copy(comparisonId = comparisonId)
        val state = DeleteComparisonState(comparison = createResource(id = comparisonId, modifiable = false))

        shouldThrow<ComparisonNotModifiable> { comparisonDeleteValidator(command, state) }
    }

    @Test
    fun `Given a comparison delete command, when comparison is published, it throws an exception`() {
        val comparisonId = ThingId("R8186")
        val command = deleteComparisonCommand().copy(comparisonId = comparisonId)
        val state = DeleteComparisonState(comparison = createResource(id = comparisonId, classes = setOf(Classes.comparisonPublished)))

        shouldThrow<ComparisonAlreadyPublished> { comparisonDeleteValidator(command, state) }
    }

    @Test
    fun `Given a comparison delete command, when comparison has published versions, it throws an exception`() {
        val comparisonId = ThingId("R8186")
        val comparison = createResource(id = comparisonId)
        val command = deleteComparisonCommand().copy(comparisonId = comparisonId)
        val state = DeleteComparisonState(
            comparison = comparison,
            statements = mapOf(
                comparisonId to listOf(
                    createStatement(
                        subject = comparison,
                        predicate = createPredicate(Predicates.hasPublishedVersion),
                        `object` = createResource(classes = setOf(Classes.comparisonPublished)),
                    ),
                ),
            ),
        )

        shouldThrow<ComparisonAlreadyPublished> { comparisonDeleteValidator(command, state) }
    }

    @Test
    fun `Given a comparison delete command, when comparison is used as an object, it throws an exception`() {
        val comparisonId = ThingId("R8186")
        val command = deleteComparisonCommand().copy(comparisonId = comparisonId)
        val state = DeleteComparisonState(comparison = createResource(id = comparisonId))

        every { thingRepository.isUsedAsObject(comparisonId) } returns true

        shouldThrow<ComparisonInUse> { comparisonDeleteValidator(command, state) }

        verify(exactly = 1) { thingRepository.isUsedAsObject(comparisonId) }
    }

    @Test
    fun `Given a comparison delete command, when comparison is null, it does nothing`() {
        val comparisonId = ThingId("R8186")
        val command = deleteComparisonCommand().copy(comparisonId = comparisonId)
        val state = DeleteComparisonState()

        comparisonDeleteValidator(command, state) shouldBe state
    }
}
