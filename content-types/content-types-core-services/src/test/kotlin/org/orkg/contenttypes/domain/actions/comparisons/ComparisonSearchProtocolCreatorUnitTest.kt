package org.orkg.contenttypes.domain.actions.comparisons

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
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
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyCreator
import org.orkg.contenttypes.input.ComparisonSearchProtocolCommand
import org.orkg.contenttypes.input.testing.fixtures.createComparisonCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateListUseCase
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ThingRepository

internal class ComparisonSearchProtocolCreatorUnitTest : MockkBaseTest {
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator = mockk()
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val listUseCases: ListUseCases = mockk()

    private val comparisonResourceCreator = ComparisonSearchProtocolCreator(
        singleStatementPropertyCreator,
        unsafeLiteralUseCases,
        unsafeStatementUseCases,
        listUseCases,
    )

    @Test
    fun `Given a comparison create command, when creating the search protocol and search protocol is empty, it does nothing`() {
        val command = createComparisonCommand().copy(
            searchProtocol = ComparisonSearchProtocolCommand(
                inclusionCriteria = null,
                exclusionCriteria = null,
                searchEngines = emptyList(),
                searchStrings = emptyList(),
                researchQuestions = emptyList(),
                numberOfStudiesOriginallyReturned = null,
                numberOfStudiesRetained = null,
            ),
        )
        val state = CreateComparisonState()

        comparisonResourceCreator(command, state) shouldBe state
    }

    @Test
    fun `Given a comparison create command, when creating the inclusion criteria of the search protocol, it returns success`() {
        val command = createComparisonCommand().copy(
            searchProtocol = ComparisonSearchProtocolCommand(
                inclusionCriteria = "has more than 5 authors",
                exclusionCriteria = null,
                searchEngines = emptyList(),
                searchStrings = emptyList(),
                researchQuestions = emptyList(),
                numberOfStudiesOriginallyReturned = null,
                numberOfStudiesRetained = null,
            ),
        )
        val state = CreateComparisonState(comparisonId = ThingId("R159"))

        every {
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.comparisonId!!,
                predicateId = Predicates.inclusionCriteria,
                label = "has more than 5 authors",
                datatype = Literals.XSD.INT.prefixedUri,
                extractionMethod = command.extractionMethod,
            )
        } just runs

        comparisonResourceCreator(command, state) shouldBe state

        verify(exactly = 1) {
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.comparisonId!!,
                predicateId = Predicates.inclusionCriteria,
                label = "has more than 5 authors",
                datatype = Literals.XSD.INT.prefixedUri,
                extractionMethod = command.extractionMethod,
            )
        }
    }

    @Test
    fun `Given a comparison create command, when creating the exclusion criteria of the search protocol, it returns success`() {
        val command = createComparisonCommand().copy(
            searchProtocol = ComparisonSearchProtocolCommand(
                inclusionCriteria = null,
                exclusionCriteria = "has long title",
                searchEngines = emptyList(),
                searchStrings = emptyList(),
                researchQuestions = emptyList(),
                numberOfStudiesOriginallyReturned = null,
                numberOfStudiesRetained = null,
            ),
        )
        val state = CreateComparisonState(comparisonId = ThingId("R159"))

        every {
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.comparisonId!!,
                predicateId = Predicates.exclusionCriteria,
                label = "has long title",
                datatype = Literals.XSD.INT.prefixedUri,
                extractionMethod = command.extractionMethod,
            )
        } just runs

        comparisonResourceCreator(command, state) shouldBe state

        verify(exactly = 1) {
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.comparisonId!!,
                predicateId = Predicates.exclusionCriteria,
                label = "has long title",
                datatype = Literals.XSD.INT.prefixedUri,
                extractionMethod = command.extractionMethod,
            )
        }
    }

    @Test
    fun `Given a comparison create command, when creating the search strings list of the search protocol, it returns success`() {
        val command = createComparisonCommand().copy(
            searchProtocol = ComparisonSearchProtocolCommand(
                inclusionCriteria = null,
                exclusionCriteria = null,
                searchEngines = emptyList(),
                searchStrings = listOf("example paper"),
                researchQuestions = emptyList(),
                numberOfStudiesOriginallyReturned = null,
                numberOfStudiesRetained = null,
            ),
        )
        val state = CreateComparisonState(comparisonId = ThingId("R159"))
        val literalId = ThingId("L123")
        val listId = ThingId("R123")

        every {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = "example paper",
                    extractionMethod = command.extractionMethod,
                ),
            )
        } returns literalId
        every {
            listUseCases.create(
                CreateListUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = "Search strings",
                    elements = listOf(literalId),
                    extractionMethod = command.extractionMethod,
                ),
            )
        } returns listId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.comparisonId!!,
                    predicateId = Predicates.searchStrings,
                    objectId = listId,
                    extractionMethod = command.extractionMethod,
                ),
            )
        } returns StatementId("S123")

        comparisonResourceCreator(command, state) shouldBe state

        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = "example paper",
                    extractionMethod = command.extractionMethod,
                ),
            )
        }
        verify(exactly = 1) {
            listUseCases.create(
                CreateListUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = "Search strings",
                    elements = listOf(literalId),
                    extractionMethod = command.extractionMethod,
                ),
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.comparisonId!!,
                    predicateId = Predicates.searchStrings,
                    objectId = listId,
                    extractionMethod = command.extractionMethod,
                ),
            )
        }
    }

    @Test
    fun `Given a comparison create command, when creating the research questions list of the search protocol, it returns success`() {
        val command = createComparisonCommand().copy(
            searchProtocol = ComparisonSearchProtocolCommand(
                inclusionCriteria = null,
                exclusionCriteria = null,
                searchEngines = emptyList(),
                searchStrings = emptyList(),
                researchQuestions = listOf("what makes a paper good"),
                numberOfStudiesOriginallyReturned = null,
                numberOfStudiesRetained = null,
            ),
        )
        val state = CreateComparisonState(comparisonId = ThingId("R159"))
        val literalId = ThingId("L123")
        val listId = ThingId("R123")

        every {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = "what makes a paper good",
                    extractionMethod = command.extractionMethod,
                ),
            )
        } returns literalId
        every {
            listUseCases.create(
                CreateListUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = "Research questions",
                    elements = listOf(literalId),
                    extractionMethod = command.extractionMethod,
                ),
            )
        } returns listId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.comparisonId!!,
                    predicateId = Predicates.researchQuestions,
                    objectId = listId,
                    extractionMethod = command.extractionMethod,
                ),
            )
        } returns StatementId("S123")

        comparisonResourceCreator(command, state) shouldBe state

        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = "what makes a paper good",
                    extractionMethod = command.extractionMethod,
                ),
            )
        }
        verify(exactly = 1) {
            listUseCases.create(
                CreateListUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = "Research questions",
                    elements = listOf(literalId),
                    extractionMethod = command.extractionMethod,
                ),
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.comparisonId!!,
                    predicateId = Predicates.researchQuestions,
                    objectId = listId,
                    extractionMethod = command.extractionMethod,
                ),
            )
        }
    }

    @Test
    fun `Given a comparison create command, when creating the number of studies originally returned, it returns success`() {
        val command = createComparisonCommand().copy(
            searchProtocol = ComparisonSearchProtocolCommand(
                inclusionCriteria = null,
                exclusionCriteria = null,
                searchEngines = emptyList(),
                searchStrings = emptyList(),
                researchQuestions = emptyList(),
                numberOfStudiesOriginallyReturned = 5,
                numberOfStudiesRetained = null,
            ),
        )
        val state = CreateComparisonState(comparisonId = ThingId("R159"))

        every {
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.comparisonId!!,
                predicateId = Predicates.numberOfStudiesOriginallyReturned,
                label = "5",
                datatype = Literals.XSD.INT.prefixedUri,
                extractionMethod = command.extractionMethod,
            )
        } just runs

        comparisonResourceCreator(command, state) shouldBe state

        verify(exactly = 1) {
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.comparisonId!!,
                predicateId = Predicates.numberOfStudiesOriginallyReturned,
                label = "5",
                datatype = Literals.XSD.INT.prefixedUri,
                extractionMethod = command.extractionMethod,
            )
        }
    }

    @Test
    fun `Given a comparison create command, when creating the number of studies retained, it returns success`() {
        val command = createComparisonCommand().copy(
            searchProtocol = ComparisonSearchProtocolCommand(
                inclusionCriteria = null,
                exclusionCriteria = null,
                searchEngines = emptyList(),
                searchStrings = emptyList(),
                researchQuestions = emptyList(),
                numberOfStudiesOriginallyReturned = null,
                numberOfStudiesRetained = 2,
            ),
        )
        val state = CreateComparisonState(comparisonId = ThingId("R159"))

        every {
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.comparisonId!!,
                predicateId = Predicates.numberOfStudiesRetained,
                label = "2",
                datatype = Literals.XSD.INT.prefixedUri,
                extractionMethod = command.extractionMethod,
            )
        } just runs

        comparisonResourceCreator(command, state) shouldBe state

        verify(exactly = 1) {
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.comparisonId!!,
                predicateId = Predicates.numberOfStudiesRetained,
                label = "2",
                datatype = Literals.XSD.INT.prefixedUri,
                extractionMethod = command.extractionMethod,
            )
        }
    }

    @Test
    fun `Given a comparison create command, when creating the search engines list of the search protocol, it returns success`() {
        val command = createComparisonCommand().copy(
            searchProtocol = ComparisonSearchProtocolCommand(
                inclusionCriteria = null,
                exclusionCriteria = null,
                searchEngines = listOf(ThingId("R85476")),
                searchStrings = emptyList(),
                researchQuestions = emptyList(),
                numberOfStudiesOriginallyReturned = null,
                numberOfStudiesRetained = null,
            ),
        )
        val state = CreateComparisonState(comparisonId = ThingId("R159"))
        val listId = ThingId("R123")

        every {
            listUseCases.create(
                CreateListUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = "Search engines",
                    elements = listOf(ThingId("R85476")),
                    extractionMethod = command.extractionMethod,
                ),
            )
        } returns listId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.comparisonId!!,
                    predicateId = Predicates.searchEngines,
                    objectId = listId,
                    extractionMethod = command.extractionMethod,
                ),
            )
        } returns StatementId("S123")

        comparisonResourceCreator(command, state) shouldBe state

        verify(exactly = 1) {
            listUseCases.create(
                CreateListUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = "Search engines",
                    elements = listOf(ThingId("R85476")),
                    extractionMethod = command.extractionMethod,
                ),
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.comparisonId!!,
                    predicateId = Predicates.searchEngines,
                    objectId = listId,
                    extractionMethod = command.extractionMethod,
                ),
            )
        }
    }
}
