package org.orkg.contenttypes.domain.actions.comparisons

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.ComparisonNotModifiable
import org.orkg.contenttypes.domain.actions.UpdateComparisonState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyComparison
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateComparisonCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf

class ComparisonModifiableValidatorUnitTest {
    private val statementService: StatementUseCases = mockk()

    private val comparisonModifiableValidator = ComparisonModifiableValidator(statementService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService)
    }

    @Test
    fun `Given a comparison update command, when comparison represents the current version, it returns success`() {
        val command = dummyUpdateComparisonCommand()
        val state = UpdateComparisonState(
            comparison = createDummyComparison()
        )

        every {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = command.comparisonId,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf()

        val result = comparisonModifiableValidator(command, state)

        result.asClue {
            it.comparison shouldBe state.comparison
            it.authors shouldBe state.authors
        }

        verify(exactly = 1) {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = command.comparisonId,
                pageable = PageRequests.SINGLE
            )
        }
    }

    @Test
    fun `Given a comparison update command, when comparison represents a previous version, it throws an exception`() {
        val command = dummyUpdateComparisonCommand()
        val state = UpdateComparisonState(
            comparison = createDummyComparison()
        )

        every {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = command.comparisonId,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf(
            createStatement(
                subject = createResource(classes = setOf(Classes.comparison)),
                predicate = createPredicate(Predicates.hasPreviousVersion),
                `object` = createResource(command.comparisonId)
            )
        )

        assertThrows<ComparisonNotModifiable> { comparisonModifiableValidator(command, state) }

        verify(exactly = 1) {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = command.comparisonId,
                pageable = PageRequests.SINGLE
            )
        }
    }
}
