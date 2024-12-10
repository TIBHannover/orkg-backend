package org.orkg.contenttypes.domain.actions.literaturelists

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
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListState
import org.orkg.contenttypes.domain.testing.fixtures.createLiteratureList
import org.orkg.contenttypes.input.testing.fixtures.dummyLiteratureListTextSectionDefinition
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateLiteratureListCommand
import org.orkg.contenttypes.input.testing.fixtures.toLiteratureListSectionDefinition
import org.orkg.graph.domain.Predicates
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

internal class LiteratureListSectionsUpdaterUnitTest {
    private val abstractLiteratureListSectionCreator: AbstractLiteratureListSectionCreator = mockk()
    private val abstractLiteratureListSectionDeleter: AbstractLiteratureListSectionDeleter = mockk()
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater = mockk()

    private val literatureListSectionsUpdater = LiteratureListSectionsUpdater(
        abstractLiteratureListSectionCreator, abstractLiteratureListSectionDeleter, statementCollectionPropertyUpdater
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(
            abstractLiteratureListSectionCreator,
            abstractLiteratureListSectionDeleter,
            statementCollectionPropertyUpdater
        )
    }

    @Test
    fun `Given a literature list update command, when sections are not set, it does nothing`() {
        val literatureList = createLiteratureList()
        val command = dummyUpdateLiteratureListCommand().copy(
            sections = null
        )
        val state = UpdateLiteratureListState(
            literatureList = literatureList
        )

        literatureListSectionsUpdater(command, state)
    }

    @Test
    fun `Given a literature list update command, when sections are unchanged, it does nothing`() {
        val literatureList = createLiteratureList()
        val command = dummyUpdateLiteratureListCommand().copy(
            sections = literatureList.sections.map { it.toLiteratureListSectionDefinition() }
        )
        val state = UpdateLiteratureListState(
            literatureList = literatureList
        )

        literatureListSectionsUpdater(command, state)
    }

    @Test
    fun `Given a literature list update command, when a section is removed, it deletes the old section`() {
        val literatureList = createLiteratureList()
        val command = dummyUpdateLiteratureListCommand().copy(
            sections = literatureList.sections.dropLast(1).map { it.toLiteratureListSectionDefinition() }
        )
        val state = UpdateLiteratureListState(
            literatureList = literatureList,
            statements = listOf(
                createStatement(subject = createResource(command.literatureListId), predicate = createPredicate(Predicates.hasSection)),
                createStatement(subject = createResource(command.literatureListId), predicate = createPredicate(Predicates.hasLink))
            ).groupBy { it.subject.id }
        )

        every {
            statementCollectionPropertyUpdater.update(
                statements = any(),
                contributorId = command.contributorId,
                subjectId = command.literatureListId,
                predicateId = Predicates.hasSection,
                objects = any<List<ThingId>>()
            )
        } just runs
        every {
            abstractLiteratureListSectionDeleter.delete(
                contributorId = command.contributorId,
                literatureListId = command.literatureListId,
                section = literatureList.sections.last(),
                statements = state.statements
            )
        } just runs

        literatureListSectionsUpdater(command, state)

        verify(exactly = 1) {
            statementCollectionPropertyUpdater.update(
                statements = state.statements[command.literatureListId].orEmpty(),
                contributorId = command.contributorId,
                subjectId = command.literatureListId,
                predicateId = Predicates.hasSection,
                objects = literatureList.sections.dropLast(1).map { it.id }
            )
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
    fun `Given a literature list update command, when a section is added, it creates a new section`() {
        val literatureList = createLiteratureList()
        val newSection = dummyLiteratureListTextSectionDefinition().copy(text = "new section")
        val command = dummyUpdateLiteratureListCommand().copy(
            sections = literatureList.sections.map { it.toLiteratureListSectionDefinition() } + newSection
        )
        val state = UpdateLiteratureListState(
            literatureList = literatureList,
            statements = listOf(
                createStatement(subject = createResource(command.literatureListId), predicate = createPredicate(Predicates.hasSection)),
                createStatement(subject = createResource(command.literatureListId), predicate = createPredicate(Predicates.hasLink))
            ).groupBy { it.subject.id }
        )
        val newSectionId = ThingId("irrelevant")

        every {
            abstractLiteratureListSectionCreator.create(command.contributorId, newSection)
        } returns newSectionId
        every {
            statementCollectionPropertyUpdater.update(
                statements = any(),
                contributorId = command.contributorId,
                subjectId = command.literatureListId,
                predicateId = Predicates.hasSection,
                objects = any<List<ThingId>>()
            )
        } just runs

        literatureListSectionsUpdater(command, state)

        verify(exactly = 1) { abstractLiteratureListSectionCreator.create(command.contributorId, newSection) }
        verify(exactly = 1) {
            statementCollectionPropertyUpdater.update(
                statements = state.statements[command.literatureListId].orEmpty(),
                contributorId = command.contributorId,
                subjectId = command.literatureListId,
                predicateId = Predicates.hasSection,
                objects = literatureList.sections.map { it.id } + newSectionId
            )
        }
    }

    @Test
    fun `Given a literature list update command, when a section is replaced, it deletes the old section and creates a new one`() {
        val literatureList = createLiteratureList()
        val newSection = dummyLiteratureListTextSectionDefinition().copy(text = "new section")
        val command = dummyUpdateLiteratureListCommand().copy(
            sections = literatureList.sections.dropLast(1).map { it.toLiteratureListSectionDefinition() } + newSection
        )
        val state = UpdateLiteratureListState(
            literatureList = literatureList,
            statements = listOf(
                createStatement(subject = createResource(command.literatureListId), predicate = createPredicate(Predicates.hasSection)),
                createStatement(subject = createResource(command.literatureListId), predicate = createPredicate(Predicates.hasLink))
            ).groupBy { it.subject.id }
        )
        val newSectionId = ThingId("irrelevant")

        every {
            abstractLiteratureListSectionCreator.create(command.contributorId, newSection)
        } returns newSectionId
        every {
            statementCollectionPropertyUpdater.update(
                statements = any(),
                contributorId = command.contributorId,
                subjectId = command.literatureListId,
                predicateId = Predicates.hasSection,
                objects = any<List<ThingId>>()
            )
        } just runs
        every {
            abstractLiteratureListSectionDeleter.delete(
                contributorId = command.contributorId,
                literatureListId = command.literatureListId,
                section = literatureList.sections.last(),
                statements = state.statements
            )
        } just runs

        literatureListSectionsUpdater(command, state)

        verify(exactly = 1) { abstractLiteratureListSectionCreator.create(command.contributorId, newSection) }
        verify(exactly = 1) {
            statementCollectionPropertyUpdater.update(
                statements = state.statements[command.literatureListId].orEmpty(),
                contributorId = command.contributorId,
                subjectId = command.literatureListId,
                predicateId = Predicates.hasSection,
                objects = literatureList.sections.dropLast(1).map { it.id } + newSectionId
            )
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
}
