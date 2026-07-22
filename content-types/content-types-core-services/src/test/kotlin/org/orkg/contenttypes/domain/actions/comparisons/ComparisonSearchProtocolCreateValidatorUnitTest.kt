package org.orkg.contenttypes.domain.actions.comparisons

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ComparisonSearchProtocol
import org.orkg.contenttypes.domain.InvalidOriginallyReturnedStudyCount
import org.orkg.contenttypes.domain.InvalidRetainedStudyCount
import org.orkg.contenttypes.domain.InvalidStudyCounts
import org.orkg.contenttypes.domain.SearchEngineEntityNotFound
import org.orkg.contenttypes.domain.actions.CreateComparisonState
import org.orkg.contenttypes.input.ComparisonSearchProtocolCommand
import org.orkg.contenttypes.input.testing.fixtures.createComparisonCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.output.ThingRepository

internal class ComparisonSearchProtocolCreateValidatorUnitTest : MockkBaseTest {
    private val thingRepository: ThingRepository = mockk()

    private val comparisonResourceCreator = ComparisonSearchProtocolCreateValidator(thingRepository)

    @Test
    fun `Given a comparison create command, when valdating the search protocol, it returns success`() {
        val command = createComparisonCommand()
        val state = CreateComparisonState()

        every { thingRepository.existsAllById(any()) } returns true

        comparisonResourceCreator(command, state) shouldBe state

        verify(exactly = 1) { thingRepository.existsAllById(command.searchProtocol.searchEngines.toSet()) }
    }

    @Test
    fun `Given a comparison create command, when valdating the search protocol and inclusion criteria is invalid, it throws an exception`() {
        val command = createComparisonCommand().let {
            it.copy(searchProtocol = it.searchProtocol.copy(inclusionCriteria = "a".repeat(MAX_LABEL_LENGTH + 1)))
        }
        val state = CreateComparisonState()

        shouldThrow<InvalidDescription> { comparisonResourceCreator(command, state) }.asClue {
            it.property shouldBe "search_protocol.inclusion_criteria"
        }
    }

    @Test
    fun `Given a comparison create command, when valdating the search protocol and exclusion criteria is invalid, it throws an exception`() {
        val command = createComparisonCommand().let {
            it.copy(searchProtocol = it.searchProtocol.copy(exclusionCriteria = "a".repeat(MAX_LABEL_LENGTH + 1)))
        }
        val state = CreateComparisonState()

        shouldThrow<InvalidDescription> { comparisonResourceCreator(command, state) }.asClue {
            it.property shouldBe "search_protocol.exclusion_criteria"
        }
    }

    @Test
    fun `Given a comparison create command, when valdating the search protocol and a search string is invalid, it throws an exception`() {
        val command = createComparisonCommand().let {
            it.copy(searchProtocol = it.searchProtocol.copy(searchStrings = listOf("a".repeat(MAX_LABEL_LENGTH + 1))))
        }
        val state = CreateComparisonState()

        shouldThrow<InvalidDescription> { comparisonResourceCreator(command, state) }.asClue {
            it.property shouldBe "search_protocol.search_strings[0]"
        }
    }

    @Test
    fun `Given a comparison create command, when valdating the search protocol and a research question is invalid, it throws an exception`() {
        val command = createComparisonCommand().let {
            it.copy(searchProtocol = it.searchProtocol.copy(researchQuestions = listOf("a".repeat(MAX_LABEL_LENGTH + 1))))
        }
        val state = CreateComparisonState()

        shouldThrow<InvalidDescription> { comparisonResourceCreator(command, state) }.asClue {
            it.property shouldBe "search_protocol.research_questions[0]"
        }
    }

    @Test
    fun `Given a comparison create command, when valdating the search protocol and originally returned study count is invalid, it throws an exception`() {
        val command = createComparisonCommand().let {
            it.copy(searchProtocol = it.searchProtocol.copy(numberOfStudiesOriginallyReturned = -4))
        }
        val state = CreateComparisonState()

        shouldThrow<InvalidOriginallyReturnedStudyCount> { comparisonResourceCreator(command, state) }
    }

    @Test
    fun `Given a comparison create command, when valdating the search protocol and retained study count is invalid, it throws an exception`() {
        val command = createComparisonCommand().let {
            it.copy(searchProtocol = it.searchProtocol.copy(numberOfStudiesRetained = -4))
        }
        val state = CreateComparisonState()

        shouldThrow<InvalidRetainedStudyCount> { comparisonResourceCreator(command, state) }
    }

    @Test
    fun `Given a comparison create command, when valdating the search protocol and retained study count is lower than originally returned study count, it throws an exception`() {
        val command = createComparisonCommand().let {
            it.copy(searchProtocol = it.searchProtocol.copy(numberOfStudiesOriginallyReturned = 2, numberOfStudiesRetained = 3))
        }
        val state = CreateComparisonState()

        shouldThrow<InvalidStudyCounts> { comparisonResourceCreator(command, state) }
    }

    @Test
    fun `Given a comparison create command, when valdating the search protocol and a provided search engine does not exist, it throws an exception`() {
        val command = createComparisonCommand().let {
            it.copy(searchProtocol = it.searchProtocol.copy(searchEngines = listOf(ThingId("missing"))))
        }
        val state = CreateComparisonState()

        every { thingRepository.existsAllById(any()) } returns false

        shouldThrow<SearchEngineEntityNotFound> { comparisonResourceCreator(command, state) }

        verify(exactly = 1) { thingRepository.existsAllById(command.searchProtocol.searchEngines.toSet()) }
    }
}
