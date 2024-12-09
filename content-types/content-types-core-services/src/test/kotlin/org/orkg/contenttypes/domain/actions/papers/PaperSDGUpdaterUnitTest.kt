package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyPaper
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdatePaperCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

internal class PaperSDGUpdaterUnitTest {
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater = mockk()

    private val paperSDGUpdater = PaperSDGUpdater(statementCollectionPropertyUpdater)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementCollectionPropertyUpdater)
    }

    @Test
    fun `Given a paper update command, when SDGs are not set, it does nothing`() {
        val paper = createDummyPaper()
        val command = dummyUpdatePaperCommand().copy(sustainableDevelopmentGoals = null)
        val state = UpdatePaperState(paper)

        val result = paperSDGUpdater(command, state)

        result.asClue {
            it.paper shouldBe state.paper
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a paper update command, when SDGs are unchanged, it does nothing`() {
        val paper = createDummyPaper().copy(
            sustainableDevelopmentGoals = setOf(
                ObjectIdAndLabel(ThingId("SDG_3"), "Good health and well-being"),
                ObjectIdAndLabel(ThingId("SDG_4"), "Quality Education")
            )
        )
        val command = dummyUpdatePaperCommand()
        val state = UpdatePaperState(paper)

        val result = paperSDGUpdater(command, state)

        result.asClue {
            it.paper shouldBe state.paper
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a paper update command, when SDGs have changed, it updates the SDG statements`() {
        val paper = createDummyPaper()
        val command = dummyUpdatePaperCommand()
        val statements = listOf(
            createStatement(
                subject = createResource(command.paperId),
                predicate = createPredicate(Predicates.sustainableDevelopmentGoal)
            ),
            createStatement(
                subject = createResource(command.paperId),
                predicate = createPredicate(Predicates.hasContent)
            ),
            createStatement(subject = createResource())
        ).groupBy { it.subject.id }
        val state = UpdatePaperState(paper, statements)

        every {
            statementCollectionPropertyUpdater.update(
                statements = any(),
                contributorId = command.contributorId,
                subjectId = command.paperId,
                predicateId = Predicates.sustainableDevelopmentGoal,
                objects = command.sustainableDevelopmentGoals!!
            )
        } just runs

        val result = paperSDGUpdater(command, state)

        result.asClue {
            it.paper shouldBe state.paper
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            statementCollectionPropertyUpdater.update(
                statements = statements[command.paperId].orEmpty(),
                contributorId = command.contributorId,
                subjectId = command.paperId,
                predicateId = Predicates.sustainableDevelopmentGoal,
                objects = command.sustainableDevelopmentGoals!!
            )
        }
    }
}
