package org.orkg.contenttypes.domain.actions.comparisons

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.time.Clock
import java.time.OffsetDateTime
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.actions.PublishComparisonState
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.input.testing.fixtures.dummyPublishComparisonCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.fixedClock

class ComparisonPublicationInfoUnitTest {
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater = mockk()
    private val clock: Clock = fixedClock

    private val comparisonPublicationInfoUpdater = ComparisonPublicationInfoUpdater(singleStatementPropertyUpdater, clock)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(singleStatementPropertyUpdater)
    }

    @Test
    fun `Given a comparison publish command, it updates the publication metadata`() {
        val comparison = createResource(classes = setOf(Classes.comparison))
        val command = dummyPublishComparisonCommand().copy(id = comparison.id)
        val state = PublishComparisonState(comparison)
        val now = OffsetDateTime.now(clock)

        every {
            singleStatementPropertyUpdater.updateRequiredProperty(
                contributorId = command.contributorId,
                subjectId = command.id,
                predicateId = any(),
                label = any(),
                datatype = any()
            )
        } just runs

        comparisonPublicationInfoUpdater(command, state).asClue {
            it.comparison shouldBe comparison
        }

        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateRequiredProperty(
                contributorId = command.contributorId,
                subjectId = comparison.id,
                predicateId = Predicates.yearPublished,
                label = now.year.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateRequiredProperty(
                contributorId = command.contributorId,
                subjectId = comparison.id,
                predicateId = Predicates.monthPublished,
                label = now.monthValue.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
    }
}
