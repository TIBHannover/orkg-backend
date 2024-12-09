package org.orkg.contenttypes.domain.actions.literaturelists

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
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyLiteratureList
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateLiteratureListCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

internal class LiteratureListResearchFieldUpdaterUnitTest {
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater = mockk()

    private val literatureListResearchFieldUpdater = LiteratureListResearchFieldUpdater(statementCollectionPropertyUpdater)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementCollectionPropertyUpdater)
    }

    @Test
    fun `Given a literature list update command, when research fields are not set, it does nothing`() {
        val literatureList = createDummyLiteratureList()
        val command = dummyUpdateLiteratureListCommand().copy(researchFields = null)
        val state = UpdateLiteratureListState(literatureList = literatureList)

        val result = literatureListResearchFieldUpdater(command, state)

        result.asClue {
            it.literatureList shouldBe state.literatureList
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a literature list update command, when research fields are unchanged, it does nothing`() {
        val literatureList = createDummyLiteratureList().copy(
            researchFields = listOf(ObjectIdAndLabel(ThingId("R12"), "Science"))
        )
        val command = dummyUpdateLiteratureListCommand()
        val state = UpdateLiteratureListState(literatureList = literatureList)

        val result = literatureListResearchFieldUpdater(command, state)

        result.asClue {
            it.literatureList shouldBe state.literatureList
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a literature list update command, when research fields have changed, it updates the research field statements`() {
        val literatureList = createDummyLiteratureList()
        val command = dummyUpdateLiteratureListCommand()
        val state = UpdateLiteratureListState(
            literatureList = literatureList,
            statements = listOf(
                createStatement(subject = createResource(command.literatureListId), predicate = createPredicate(Predicates.hasResearchField)),
                createStatement(subject = createResource(command.literatureListId), predicate = createPredicate(Predicates.hasContent)),
                createStatement(subject = createResource())
            ).groupBy { it.subject.id }
        )

        every {
            statementCollectionPropertyUpdater.update(
                statements = any(),
                contributorId = command.contributorId,
                subjectId = command.literatureListId,
                predicateId = Predicates.hasResearchField,
                objects = command.researchFields!!.toSet()
            )
        } just runs

        val result = literatureListResearchFieldUpdater(command, state)

        result.asClue {
            it.literatureList shouldBe state.literatureList
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            statementCollectionPropertyUpdater.update(
                statements = state.statements[command.literatureListId]!!,
                contributorId = command.contributorId,
                subjectId = command.literatureListId,
                predicateId = Predicates.hasResearchField,
                objects = command.researchFields!!.toSet()
            )
        }
    }
}
