package org.orkg.contenttypes.domain.actions.literaturelists.sections

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
import org.orkg.contenttypes.domain.actions.DeleteLiteratureListSectionState
import org.orkg.contenttypes.domain.actions.literaturelists.AbstractLiteratureListSectionDeleter
import org.orkg.contenttypes.domain.testing.fixtures.createDummyLiteratureList
import org.orkg.contenttypes.input.testing.fixtures.dummyDeleteSectionCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

class LiteratureListSectionDeleterUnitTest {
    private val abstractLiteratureListSectionDeleter: AbstractLiteratureListSectionDeleter = mockk()

    private val literatureListSectionDeleter = LiteratureListSectionDeleter(abstractLiteratureListSectionDeleter)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(abstractLiteratureListSectionDeleter)
    }

    @Test
    fun `Given a literature list section delete command, when section belongs to literature list, it deletes the section`() {
        val literatureList = createDummyLiteratureList()
        val command = dummyDeleteSectionCommand().copy(sectionId = literatureList.sections.last().id)
        val statements = listOf(
            createStatement(
                subject = createResource(command.literatureListId),
                predicate = createPredicate(Predicates.hasSection),
                `object` = createResource(literatureList.sections.first().id)
            ),
            createStatement(
                subject = createResource(command.literatureListId),
                predicate = createPredicate(Predicates.hasSection),
                `object` = createResource(literatureList.sections.last().id)
            )
        )
        val state = DeleteLiteratureListSectionState().copy(
            literatureList = literatureList,
            statements = statements.groupBy { it.subject.id }
        )

        every {
            abstractLiteratureListSectionDeleter.delete(
                contributorId = command.contributorId,
                literatureListId = command.literatureListId,
                section = literatureList.sections.last(),
                statements = state.statements
            )
        } just runs

        val result = literatureListSectionDeleter(command, state)

        result.asClue {
            it.literatureList shouldBe state.literatureList
            it.statements shouldBe state.statements
        }

        verify(exactly = 1) {
            abstractLiteratureListSectionDeleter.delete(
                contributorId = command.contributorId,
                literatureListId = command.literatureListId,
                section = literatureList.sections.last(),
                statements = state.statements
            )
        }
    }

    @Test
    fun `Given a literature list section delete command, when section does not belong to literature list, it does nothing`() {
        val literatureList = createDummyLiteratureList()
        val command = dummyDeleteSectionCommand().copy(sectionId = ThingId("R123"))
        val statements = listOf(
            createStatement(
                subject = createResource(command.literatureListId),
                predicate = createPredicate(Predicates.hasSection),
                `object` = createResource(literatureList.sections.first().id)
            ),
            createStatement(
                subject = createResource(command.literatureListId),
                predicate = createPredicate(Predicates.hasSection),
                `object` = createResource(literatureList.sections.last().id)
            )
        )
        val state = DeleteLiteratureListSectionState().copy(
            literatureList = literatureList,
            statements = statements.groupBy { it.subject.id }
        )

        val result = literatureListSectionDeleter(command, state)

        result.asClue {
            it.literatureList shouldBe state.literatureList
            it.statements shouldBe state.statements
        }
    }
}
