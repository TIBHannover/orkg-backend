package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ResourceReference
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.domain.testing.fixtures.createPaper
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdatePaperCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

internal class PaperMentioningsUpdaterUnitTest : MockkBaseTest {
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater = mockk()

    private val paperMentioningsUpdater = PaperMentioningsUpdater(statementCollectionPropertyUpdater)

    @Test
    fun `Given a paper update command, when mentionings are not set, it does nothing`() {
        val paper = createPaper()
        val command = dummyUpdatePaperCommand().copy(mentionings = null)
        val state = UpdatePaperState(paper)

        val result = paperMentioningsUpdater(command, state)

        result.asClue {
            it.paper shouldBe state.paper
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a paper update command, when mentionings are unchanged, it does nothing`() {
        val paper = createPaper().copy(
            mentionings = setOf(
                ResourceReference(
                    id = ThingId("R591"),
                    label = "Famous Comparison",
                    classes = setOf(Classes.comparison)
                ),
                ResourceReference(
                    id = ThingId("R357"),
                    label = "Some Paper",
                    classes = setOf(Classes.paper)
                )
            )
        )
        val command = dummyUpdatePaperCommand()
        val state = UpdatePaperState(paper)

        val result = paperMentioningsUpdater(command, state)

        result.asClue {
            it.paper shouldBe state.paper
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a paper update command, when mentionings have changed, it updates the mentions statements`() {
        val paper = createPaper()
        val command = dummyUpdatePaperCommand()
        val statements = listOf(
            createStatement(
                subject = createResource(command.paperId),
                predicate = createPredicate(Predicates.mentions)
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
                predicateId = Predicates.mentions,
                objects = command.mentionings!!
            )
        } just runs

        val result = paperMentioningsUpdater(command, state)

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
                predicateId = Predicates.mentions,
                objects = command.mentionings!!
            )
        }
    }
}
